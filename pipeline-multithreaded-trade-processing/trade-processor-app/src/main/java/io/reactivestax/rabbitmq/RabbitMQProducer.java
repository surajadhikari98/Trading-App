package io.reactivestax.rabbitmq;

import com.rabbitmq.client.Channel;
import io.reactivestax.infra.Infra;
import io.reactivestax.utils.Utility;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RabbitMQProducer {

    private static final String EXCHANGE_NAME = "trades";
    static ConcurrentHashMap<String, Integer> queueDistributorMap = new ConcurrentHashMap<>();


    // Get the queue number, or assign one in a round-robin or random manner based on application-properties
    public static void figureTheNextQueue(String[] trade, Channel channel) throws InterruptedException, IOException {
        String distributionCriteria = Infra.readFromApplicationPropertiesStringFormat("distributionLogic.Criteria");
        String useMap = Infra.readFromApplicationPropertiesStringFormat("distributionLogic.useMap");
        String distributionAlgorithm = Infra.readFromApplicationPropertiesStringFormat("distributionLogic.algorithm");


        if (Boolean.parseBoolean(useMap)) {
            int partitionNumber = 0;
            if (Objects.equals(distributionAlgorithm, "round-robin")) {
                partitionNumber = queueDistributorMap.computeIfAbsent(distributionCriteria.equals("accountNumber") ? trade[2] : trade[0],
                        k -> Utility.roundRobin()); //generate 1,2,3
            } else if (Objects.equals(distributionAlgorithm, "random")) {
                partitionNumber = queueDistributorMap.computeIfAbsent(distributionCriteria.equals("accountNumber") ? trade[2] : trade[0],
                        k -> Utility.random()); //generate 1,2,3
            }
            selectAndPublish(trade[0], partitionNumber, channel);
            log.info("Assigned trade ID: {} to queue: {}",  trade[0], partitionNumber);
        }

        if (!Boolean.parseBoolean(useMap)) {
            int queueNumber = 0;
            if (distributionAlgorithm.equals("round-robin")) {
                queueNumber = Utility.roundRobin();
            }

            if (distributionAlgorithm.equals("random")) {
                queueNumber = Utility.random();
            }
            selectAndPublish(trade[0], queueNumber, channel);
            log.info("Assigned trade ID {} to queue {} {}", trade[0], trade[0], queueNumber);
        }
    }

    private static void selectAndPublish(String tradeId, Integer queueNumber, Channel channel) throws InterruptedException, IOException {
        String routingKey = "cc_partition_" + (queueNumber - 1);
        channel.basicPublish(EXCHANGE_NAME, routingKey, null, tradeId.getBytes("UTF-8"));
        System.out.println(" [x] Sent '" + tradeId + "' with routing key '" + routingKey + "'");
    }

}
