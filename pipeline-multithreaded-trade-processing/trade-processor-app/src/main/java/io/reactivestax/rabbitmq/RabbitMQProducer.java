package io.reactivestax.rabbitmq;

import com.rabbitmq.client.Channel;
import io.reactivestax.infra.Infra;
import io.reactivestax.utils.Utility;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static io.reactivestax.infra.Infra.readFromApplicationPropertiesStringFormat;

@Slf4j
public class RabbitMQProducer {

    static ConcurrentHashMap<String, Integer> queueDistributorMap = new ConcurrentHashMap<>();


    // Get the queue number, or assign one in a round-robin or random manner based on application-properties
    public static void figureTheNextQueue(String[] trade, Channel channel) throws InterruptedException, IOException {
        String distributionCriteria = readFromApplicationPropertiesStringFormat("distributionLogic.Criteria");
        String useMap = readFromApplicationPropertiesStringFormat("distributionLogic.useMap");
        String distributionAlgorithm = readFromApplicationPropertiesStringFormat("distributionLogic.algorithm");


        if (Boolean.parseBoolean(useMap)) {
            int partitionNumber = 0;
            if (Objects.equals(distributionAlgorithm, "round-robin")) {
                partitionNumber = queueDistributorMap.computeIfAbsent(distributionCriteria.equals("accountNumber") ? trade[2] : trade[0],
                        k -> Utility.roundRobin()); //generate 1,2,3
            } else if (Objects.equals(distributionAlgorithm, "random")) {
                partitionNumber = queueDistributorMap.computeIfAbsent(distributionCriteria.equals("accountNumber") ? trade[2] : trade[0],
                        k -> Utility.random()); //generate 1,2,3
            }
            selectAndPublishToMQ(trade[0], partitionNumber, channel);
            log.info("Assigned trade ID: {} to queue: {}", trade[0], partitionNumber);
        }

        if (!Boolean.parseBoolean(useMap)) {
            int queueNumber = 0;
            if (distributionAlgorithm.equals("round-robin")) {
                queueNumber = Utility.roundRobin();
            }

            if (distributionAlgorithm.equals("random")) {
                queueNumber = Utility.random();
            }
            selectAndPublishToMQ(trade[0], queueNumber, channel);
            log.info("Assigned trade ID {} to queue {} {}", trade[0], trade[0], queueNumber);
        }
    }

    private static void selectAndPublishToMQ(String tradeId, Integer queueNumber, Channel channel) throws IOException {
        String routingKey = Infra.readFromApplicationPropertiesStringFormat("rabbitMQ.queue.name") + (queueNumber - 1);
        channel.basicPublish(
                readFromApplicationPropertiesStringFormat("rabbitMQ.exchange.name"),
                routingKey,
                null,
                tradeId.getBytes(StandardCharsets.UTF_8)
        );
        System.out.println(" [x] Sent '" + tradeId + "' with routing key '" + routingKey + "'");
    }
}
