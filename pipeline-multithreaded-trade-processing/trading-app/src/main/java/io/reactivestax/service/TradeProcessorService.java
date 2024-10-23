package io.reactivestax.service;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import io.reactivestax.contract.MessageSender;
import io.reactivestax.contract.QueueSetup;
import io.reactivestax.contract.TradeProcessor;
import io.reactivestax.contract.repository.JournalEntryRepository;
import io.reactivestax.contract.repository.PositionRepository;
import io.reactivestax.contract.repository.SecuritiesReferenceRepository;
import io.reactivestax.infra.Infra;
import io.reactivestax.model.Trade;
import io.reactivestax.utils.RabbitMQUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static io.reactivestax.factory.BeanFactory.*;
import static io.reactivestax.infra.Infra.readFromApplicationPropertiesStringFormat;
import static io.reactivestax.utils.Utility.prepareTrade;

@Slf4j
public class TradeProcessorService implements Callable<Void>, TradeProcessor {
    private final LinkedBlockingDeque<String> dlQueue = new LinkedBlockingDeque<>();
    static AtomicInteger countSec = new AtomicInteger(0);
    static AtomicInteger messageCounter = new AtomicInteger(0);
    private final String queueName;

    public TradeProcessorService(String queueName) {
        this.queueName = queueName;
    }


    @Override
    public Void call() {
        try {
            processTrade();
        } catch (Exception e) {
            TradeProcessorService.log.info("trade processor:  {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void processTrade() throws Exception {
        QueueSetup queueSetUp = getQueueSetUp();
        assert queueSetUp != null;
        queueSetUp.setUpQueue(queueName);
//        Channel channel = RabbitMQUtils.getRabbitMQChannel();
//        log.info(" [*] Waiting for messages in '{}'.", queueName);
//        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//            String message = null;
//            try {
//                message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//                log.info(" [x] Received: '{} ", message);
//                processJournalWithPosition(message);
//                log.info(" [x] Processing message: {}", message);
//
//                // Acknowledge successful processing
//                int currentCount = messageCounter.incrementAndGet();
//                log.info(" [x] Total messages consumed: {} from {}", currentCount, queueName);
//                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//            } catch (Exception e) {
//                // Check the number of retries from the headers
//                Map<String, Object> headers = delivery.getProperties().getHeaders();
//
//                int retries = headers != null && headers.containsKey("x-retries")
//                        ? (int) headers.get("x-retries")
//                        : 0;
//
//                if (retries >= Infra.readFromApplicationPropertiesIntegerFormat("max.retry.count")) {
//                    log.info(" [x] Max retries reached: {} . Discarding message: {}", retries, message);
//                    try {
//                       log.info("rejected");
//                        channel.basicPublish("dead-letter-exchange", "dead-routing-key", null, delivery.getBody());
//                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//                    } catch (Exception ex) {
//                        log.error(e.getMessage());
//                    }
//                } else {
//                    // Retry message
//                    AMQP.BasicProperties retryProps = new AMQP.BasicProperties.Builder()
//                            .headers(Map.of("x-retries", retries + 1)) // Increment retry count
//                            .build();
//
//                    channel.basicPublish(readFromApplicationPropertiesStringFormat("queue.dlx.exchange"),
//                            delivery.getEnvelope().getRoutingKey(), retryProps, delivery.getBody());
//
//                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//                    log.info(" [x] Retrying message: {} Retry # {}", message, (retries + 1));
//                }
//            }
//            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//            log.info("Position insertion successful ===========>");
//        };
//        //second parameter false means auto-acknowledge is false
//        channel.basicConsume(queueName, false, deliverCallback,consumerTag -> {});
//        // Use a CountDownLatch to wait indefinitely
//        CountDownLatch latch = new CountDownLatch(1);
//        latch.await(); // This will block the main thread forever until countDown() is called
    }

    public static void processJournalWithPosition(String tradeId) throws FileNotFoundException, SQLException {
        String payload = getTradePayloadRepository().readTradePayloadByTradeId(tradeId);
        SecuritiesReferenceRepository lookupSecuritiesRepository = getLookupSecuritiesRepository();
        JournalEntryRepository journalEntryRepository = getJournalEntryRepository();
        Trade trade = prepareTrade(payload);
        log.info("result journal{}", payload);
        try {
            if (!lookupSecuritiesRepository.lookupSecurities(trade.getCusip())) {
                log.warn("No security found....");
//                dlQueue.put(trade.getTradeIdentifier());
                log.debug("times {} {}", trade.getCusip(), countSec.incrementAndGet());
                throw new Exception(); // For checking the max retry mechanism.....
            } else {
                journalEntryRepository.saveJournalEntry(trade);
                processPosition(trade);
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage());
        }
    }


    public static void processPosition(Trade trade) throws Exception {
        PositionRepository positionsRepository = getPositionsRepository();
        Integer version = positionsRepository.getCusipVersion(trade);
        if (version != null) {
            positionsRepository.updatePosition(trade, version);
        } else {
            positionsRepository.insertPosition(trade);
        }
    }

}
