package io.reactivestax.dlq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import io.reactivestax.contract.repository.JournalEntryRepository;
import io.reactivestax.contract.repository.PositionRepository;
import io.reactivestax.contract.repository.SecuritiesReferenceRepository;
import io.reactivestax.domain.Trade;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static io.reactivestax.factory.BeanFactory.*;
import static io.reactivestax.factory.BeanFactory.getPositionsRepository;
import static io.reactivestax.utils.Utility.prepareTrade;

@Slf4j
public class CustomDeliverCallback implements DeliverCallback {

    private final Channel channel;
    private final AtomicInteger messageCounter;
    private final int MAX_RETRIES;
    private final String DLX_EXCHANGE;
    static AtomicInteger countSec = new AtomicInteger(0);

    public CustomDeliverCallback(Channel channel, AtomicInteger messageCounter, int MAX_RETRIES, String DLX_EXCHANGE) {
        this.channel = channel;
        this.messageCounter = messageCounter;
        this.MAX_RETRIES = MAX_RETRIES;
        this.DLX_EXCHANGE = DLX_EXCHANGE;
    }

    @Override
    public void handle(String consumerTag, com.rabbitmq.client.Delivery delivery) throws IOException {
        String message = null;
        try {
            message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received: '" + message + "'");
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            // Simulate processing of the message

            // Simulate failure for certain messages

            if (checkRetryError()) {
                System.out.println("$$$$ =>>>>> Retry count ====>>> ");
                throw new Exception("Simulated processing failure");
            } else {

                processJournalWithPosition(message);
                System.out.println(" [x] Processing message: " + message);

                // Acknowledge successful processing
                int currentCount = messageCounter.incrementAndGet();
                System.out.println(" [x] Total messages consumed: " + currentCount);
            }

        } catch (Exception e) {
            // Check the number of retries from the headers
            Map<String, Object> headers = delivery.getProperties().getHeaders();
            int retries = headers != null && headers.containsKey("x-retries")
                    ? (int) headers.get("x-retries")
                    : 0;

            if (retries >= MAX_RETRIES) {
                // Discard message after max retries
                System.out.println(" [x] Max retries reached: " + retries + ". Discarding message: " + message);
                try {
                    // Acknowledge to discard message
                    //Max count reached so it will go to the DLQueue........
//                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                    channel.basicPublish(DLX_EXCHANGE, "dlx_routing_key", null, delivery.getBody());
                    //NOTE: In REAL-WORLD, you will log it to failed-trades.txt file
                    // and then email it to the trading-admin at the EOD
                    // for manual intervention
                } catch (Exception ex) {
                    log.error(e.getMessage());
                }
            } else {
                // Retry message
                AMQP.BasicProperties retryProps = new AMQP.BasicProperties.Builder()
                        .headers(Map.of("x-retries", retries + 1)) // Increment retry count
                        .build();

                    // Re-publish to DLX (will be re-queued back to the main queue after TTL)
                    channel.basicPublish(DLX_EXCHANGE, "dlx_routing_key", retryProps, delivery.getBody());

                    // Reject message (will go to DLX)
//                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                    System.out.println(" [x] Retrying message: " + message + ", Retry #" + (retries + 1));
                }
//            channel.basicReject(delivery.getEnvelope().getDeliveryTag(), false); // false means the message won't be requed
            }
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
                log.debug("times {} {}", trade.getCusip(), countSec.incrementAndGet());
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

    private boolean checkRetryError(){
        return Math.random() > 0.9;
    }
}
