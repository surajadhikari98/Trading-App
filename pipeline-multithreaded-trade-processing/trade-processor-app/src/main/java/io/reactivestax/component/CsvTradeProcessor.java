package io.reactivestax.component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import io.reactivestax.contract.TradeProcessor;
import io.reactivestax.domain.Trade;
import io.reactivestax.exception.OptimisticLockingException;
import io.reactivestax.hikari.DataSource;
import io.reactivestax.infra.Infra;
import io.reactivestax.repository.TradePositionRepository;
import io.reactivestax.repository.CsvTradeProcessorRepository;
import io.reactivestax.repository.hibernate.crud.JournalEntryCRUD;
import io.reactivestax.repository.hibernate.crud.TradePayloadCRUD;
import io.reactivestax.repository.hibernate.crud.TradePositionCRUD;
import io.reactivestax.utils.HibernateUtil;
import io.reactivestax.utils.RabbitMQUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class CsvTradeProcessor implements Runnable, TradeProcessor {
    private final LinkedBlockingDeque<String> dequeue;
    private final LinkedBlockingDeque<String> dlQueue = new LinkedBlockingDeque<>();
    Map<String, Integer> retryMapper = new ConcurrentHashMap<>();
    static AtomicInteger countSec = new AtomicInteger(0);


    private final static String EXCHANGE_NAME = "trades";

    public CsvTradeProcessor(LinkedBlockingDeque<String> dequeue) {
        this.dequeue = dequeue;
    }

    @Override
    public void run() {
        try {
            processTrade();
        } catch (Exception e) {
            CsvTradeProcessor.log.info("trade processor:  {}", e.getMessage());
        }
    }

    @Override
    public void processTrade() throws Exception {
        CsvTradeProcessorRepository csvTradeProcessorRepository = new CsvTradeProcessorRepository(DataSource.getConnection());
        int partitionNumber = Infra.readFromApplicationPropertiesIntegerFormat("numberOfQueues");
        // Declare an exchange and queue, then bind them
        Channel channel = RabbitMQUtils.createChannel();

        // Declare an exchange and a single queue
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        // Declare a single queue
        String queueName = "all_partitions_queue";
        channel.queueDeclare(queueName, true, false, false, null);

        // Bind the queue to all three partitions
        for (int i = 0; i < partitionNumber; i++) {
            channel.queueBind(queueName, EXCHANGE_NAME, "cc_partition_" + i);
        }

        System.out.println(" [*] Waiting for messages in '" + queueName + "'.");

        // Callback to handle the messages
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String tradeId = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + tradeId + "' with routing key '" + delivery.getEnvelope().getRoutingKey() + "'");
            // Add logic here to process the transaction
            String payload = TradePayloadCRUD.readTradePayloadByTradeId(tradeId);
            String[] payloads = payload.split(",");
            Trade trade = new Trade(payloads[0], payloads[1], payloads[2], payloads[3], payloads[4], Integer.parseInt(payloads[5]), Double.parseDouble(payloads[6]), Integer.parseInt(payloads[5]));
            CsvTradeProcessor.log.info("Result journal{}", payload);
            System.out.println("result journal" + payload);
            try {
                if (!csvTradeProcessorRepository.lookUpSecurityByCUSIP(trade.getCusip())) {
                    log.warn("No security found....");
                    dlQueue.put(trade.getTradeIdentifier());
                    log.debug("times {} {}", trade.getCusip(), countSec.incrementAndGet());
                } else {
                    JournalEntryCRUD.persistJournalEntry(trade);
//                    processPosition(trade);
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

            // Start consuming messages
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});

        // Use a CountDownLatch to wait indefinitely
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(); // This will block the main thread forever until countDown() is called
    }


//    public void processPosition(Trade trade) throws SQLException, InterruptedException {
//            Integer version = TradePositionCRUD.getCusipVersion(trade);
//            if (version != null) {
//                TradePositionCRUD.updatePosition(trade, version);
//            } else {
//                TradePositionCRUD.persistPosition(trade);
//            }
//    }

    public int getDlQueueSize() {
        return this.dlQueue.size();
    }
}
