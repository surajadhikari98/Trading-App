package io.reactivestax.service;

import io.reactivestax.infra.Infra;
import io.reactivestax.utils.RabbitMQUtils;
import io.reactivestax.utils.Utility;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import static factory.BeanFactory.getQueueMessageSender;
import static io.reactivestax.infra.Infra.readFromApplicationPropertiesStringFormat;

@Slf4j
public class QueueDistributor {

    // Get the queue number, or assign one in a round-robin or random manner based on application-properties
    public static void figureTheNextQueue(String[] trade) throws InterruptedException, IOException, TimeoutException {

        ConcurrentHashMap<String, Integer> queueDistributorMap = new ConcurrentHashMap<>();
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
            String queueName = readFromApplicationPropertiesStringFormat("queue.name") + (partitionNumber - 1);
            getQueueMessageSender().sendMessageToQueue(queueName,trade[0]);
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
            String queueName = readFromApplicationPropertiesStringFormat("queue.name") + (queueNumber - 1);
            getQueueMessageSender().sendMessageToQueue(queueName,trade[0]);
            log.info("Assigned trade ID {} to queue {} {}", trade[0], trade[0], queueNumber);
        }
    }
}
