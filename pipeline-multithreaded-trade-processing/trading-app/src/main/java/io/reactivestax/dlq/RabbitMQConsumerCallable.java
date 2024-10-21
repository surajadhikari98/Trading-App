package io.reactivestax.dlq;

import com.rabbitmq.client.*;
import io.reactivestax.contract.repository.JournalEntryRepository;
import io.reactivestax.contract.repository.PositionRepository;
import io.reactivestax.contract.repository.SecuritiesReferenceRepository;
import io.reactivestax.domain.Trade;
import io.reactivestax.infra.Infra;
import io.reactivestax.utils.RabbitMQUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static io.reactivestax.factory.BeanFactory.*;
import static io.reactivestax.factory.BeanFactory.getPositionsRepository;
import static io.reactivestax.infra.Infra.readFromApplicationPropertiesIntegerFormat;
import static io.reactivestax.infra.Infra.readFromApplicationPropertiesStringFormat;
import static io.reactivestax.utils.Utility.prepareTrade;

@Slf4j
public class RabbitMQConsumerCallable implements Callable<Void> {

    private final static String DLX_QUEUE;
    private final static String MAIN_EXCHANGE;
    private final static String DLX_EXCHANGE;
    private final static int MAX_RETRIES;

    static {
        try {
            DLX_QUEUE = readFromApplicationPropertiesStringFormat("queue.dlx");
            MAIN_EXCHANGE = readFromApplicationPropertiesStringFormat("queue.exchange.name");
            DLX_EXCHANGE = readFromApplicationPropertiesStringFormat("queue.dlx.exchange");
            MAX_RETRIES = readFromApplicationPropertiesIntegerFormat("max.retry.count");
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    // Counter for the number of messages consumed
    private AtomicInteger messageCounter = new AtomicInteger(0);

    private final String queueName;

    public RabbitMQConsumerCallable(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public Void call() throws Exception {
        try {
            Channel channel = RabbitMQUtils.getRabbitMQChannel();

            // Declare Dead Letter Exchange (DLX) and Queue (DLQ)
            channel.exchangeDeclare(DLX_EXCHANGE, "direct");

            // DLQ should have a TTL and dead-lettering back to the main queue
            Map<String, Object> dlqArguments = new HashMap<>();
            dlqArguments.put("x-message-ttl", 5000); // Retry delay in milliseconds (5 seconds)
            dlqArguments.put("x-dead-letter-exchange", MAIN_EXCHANGE); // Requeue to main exchange
            dlqArguments.put("x-dead-letter-routing-key", "main_routing_key"); // Requeue to the main queue

            // Declare DLQ with TTL
            channel.queueDeclare(DLX_QUEUE, true, false, false, dlqArguments);
            channel.queueBind(DLX_QUEUE, DLX_EXCHANGE, "dlx_routing_key");

            // Declare the main Quorum Queue with DLX routing
            Map<String, Object> mainQueueArguments = new HashMap<>();
            mainQueueArguments.put("x-queue-type", "quorum"); // Declare quorum queue
            mainQueueArguments.put("x-dead-letter-exchange", DLX_EXCHANGE); // If a message is rejected, send to DLX
            mainQueueArguments.put("x-dead-letter-routing-key", "dlx_routing_key");

            channel.queueDeclare(queueName, true, false, false, mainQueueArguments);
            channel.queueBind(queueName, MAIN_EXCHANGE, queueName);

            System.out.println(" [*] Waiting for messages in '" + queueName + "'.");

            //NOTE: pre-Java8 separate class style
            // Use the CustomDeliverCallback class
            DeliverCallback deliverCallback = new CustomDeliverCallback(channel, messageCounter, MAX_RETRIES, DLX_EXCHANGE);

            //NOTE: Java8 lambda style
            //  DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            //
            //  }

            CancelCallback cancelCallback = new CustomCancelCallback();

            // Start consuming messages with manual acknowledgment
            channel.basicConsume(queueName, false, deliverCallback, cancelCallback);

            // Block the thread indefinitely
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(1000);
            }

        } catch (InterruptedException e) {
            System.out.println("Consumer interrupted, shutting down...");
        }
        return null;
    }

    private void processJournalWithPosition(String tradeId) throws FileNotFoundException, SQLException {
        String payload = getTradePayloadRepository().readTradePayloadByTradeId(tradeId);
        SecuritiesReferenceRepository lookupSecuritiesRepository = getLookupSecuritiesRepository();
        JournalEntryRepository journalEntryRepository = getJournalEntryRepository();
        Trade trade = prepareTrade(payload);
        log.info("result journal{}", payload);
        try {
            if (!lookupSecuritiesRepository.lookupSecurities(trade.getCusip())) {
                log.warn("No security found....");
//                dlQueue.put(trade.getTradeIdentifier());
//                log.debug("times {} {}", trade.getCusip(), countSec.incrementAndGet());
            } else {
                journalEntryRepository.saveJournalEntry(trade);
                processPosition(trade);
            }

        } catch (SQLException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage());
        }
    }


    public void processPosition(Trade trade) throws SQLException, FileNotFoundException {
        PositionRepository positionsRepository = getPositionsRepository();
        Integer version = positionsRepository.getCusipVersion(trade);
        if (version != null) {
            positionsRepository.updatePosition(trade, version);
        } else {
            positionsRepository.insertPosition(trade);
        }
    }
}

