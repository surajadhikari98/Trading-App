package io.reactivestax.component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import io.reactivestax.contract.TradeProcessor;
import io.reactivestax.domain.Trade;
import io.reactivestax.hikari.DataSource;
import io.reactivestax.infra.Infra;
import io.reactivestax.repository.CsvTradeProcessorRepository;
import io.reactivestax.repository.hibernate.crud.JournalEntryCRUD;
import io.reactivestax.repository.hibernate.crud.TradePayloadCRUD;
import io.reactivestax.repository.hibernate.crud.TradePositionCRUD;
import io.reactivestax.utils.RabbitMQUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CsvTradeProcessor implements Runnable, TradeProcessor {
    private final LinkedBlockingDeque<String> dlQueue = new LinkedBlockingDeque<>();
    static AtomicInteger countSec = new AtomicInteger(0);


    private static final String EXCHANGE_NAME = "trades";

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
        try (Channel channel = RabbitMQUtils.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            String queueName = "all_partitions_queue";
            channel.queueDeclare(queueName, true, false, false, null);

            // Bind the queue to all three partitions
            for (int i = 0; i < partitionNumber; i++) {
                channel.queueBind(queueName, EXCHANGE_NAME, "cc_partition_" + i);
            }

            log.info(" [*] Waiting for messages in '{}'.", queueName);

            // Callback to handle the messages
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String tradeId = new String(delivery.getBody(), StandardCharsets.UTF_8);
                log.info(" [x] Received '{}' with routing key '{}'", tradeId, delivery.getEnvelope().getRoutingKey());
                processJournalWithPosition(tradeId, csvTradeProcessorRepository);
            };
            // Start consuming messages
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});

            // Use a CountDownLatch to wait indefinitely
            CountDownLatch latch = new CountDownLatch(1);
            latch.await(); // This will block the main thread forever until countDown() is called
        }
    }

    private void processJournalWithPosition(String tradeId, CsvTradeProcessorRepository csvTradeProcessorRepository) {
        String payload = TradePayloadCRUD.readTradePayloadByTradeId(tradeId);
        String[] payloads = payload.split(",");
        Trade trade = new Trade(payloads[0], payloads[1], payloads[2], payloads[3], payloads[4], Integer.parseInt(payloads[5]), Double.parseDouble(payloads[6]), Integer.parseInt(payloads[5]));
        log.info("result journal{}", payload);
        try {
            if (!csvTradeProcessorRepository.lookUpSecurityByCUSIP(trade.getCusip())) {
                log.warn("No security found....");
                dlQueue.put(trade.getTradeIdentifier());
                log.debug("times {} {}", trade.getCusip(), countSec.incrementAndGet());
            } else {
                JournalEntryCRUD.persistJournalEntry(trade);
                processPosition(trade);
            }

        } catch (SQLException e) {
            log.error(e.getMessage());
        } catch (InterruptedException e) {
          log.error(e.getMessage());
        }
    }


    public void processPosition(Trade trade) throws SQLException, InterruptedException {
        Integer version = TradePositionCRUD.getCusipVersion(trade);
        if (version != null) {
            TradePositionCRUD.updatePosition(trade, version);
        } else {
            TradePositionCRUD.persistPosition(trade);
        }
    }

    public int getDlQueueSize() {
        return this.dlQueue.size();
    }
}
