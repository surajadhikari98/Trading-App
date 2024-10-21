package io.reactivestax.infra;

import com.rabbitmq.client.Channel;
import io.reactivestax.utils.RabbitMQUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

@Slf4j
public class Infra {

    private Infra() {
    }

    @Getter
    private static final LinkedBlockingQueue<String> chunksFileMappingQueue = new LinkedBlockingQueue<>();
    @Getter
    private static final List<LinkedBlockingDeque<String>> QUEUE_LIST = new ArrayList<>();
    private static final Map<String, LinkedBlockingDeque<String>> QUEUE_MAP = new HashMap<>();
    private static final Integer QUEUES_NUMBER;

    static {
        QUEUES_NUMBER = readFromApplicationPropertiesIntegerFormat("number.queues");
    }

//    public static void setUpQueue() throws IOException, TimeoutException {
//        Channel channel = RabbitMQUtils.getRabbitMQChannel();
//        String exchangeName = readFromApplicationPropertiesStringFormat("queue.exchange.name");
//        Map<String, Object> args = new HashMap<>();
//        args.put("x-dead-letter-exchange", exchangeName);
//        args.put("x-dead-letter-routing-key", "dead");
//        args.put("x-queue-type", "quorum");
//        args.put("x-delivery-limit", readFromApplicationPropertiesStringFormat("max.retry.count"));
//
//        System.out.println("RabbitMQ exchange created " + exchangeName);
//        for (int i = 0; i < readFromApplicationPropertiesIntegerFormat("number.queues"); i++) {
//            String queueName = readFromApplicationPropertiesStringFormat("queue.name") + i;
//            channel.queueDeclare(queueName, true, false, false, args);
//            channel.queueBind(queueName, exchangeName, queueName);
//            System.out.println("Queue created for = " + queueName);
//        }
//    }



    public static String readFromApplicationPropertiesStringFormat(String propertyName) throws FileNotFoundException {
        Properties properties = new Properties();
        String propName = "";

        // Use class loader to load the file from the resources folder
        try (InputStream inputStream = Infra.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                throw new FileNotFoundException("Property file 'application.properties' not found in the classpath");
            }

            properties.load(inputStream);
            return properties.getProperty(propertyName);
        } catch (IOException e) {
            System.out.println("Error reading application properties: " + e.getMessage());
        }

        return propName;
    }


    public static int readFromApplicationPropertiesIntegerFormat(String propertyName) {
        Properties properties = new Properties();

        // Use class loader to load the file from the resources folder
        try (InputStream inputStream = Infra.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                throw new FileNotFoundException("Property file 'application.properties' not found in the classpath");
            }

            properties.load(inputStream);
            return Integer.parseInt(properties.getProperty(propertyName));
        } catch (IOException e) {
            System.out.println("Error reading application properties: " + e.getMessage());
        }

        return 0;
    }

    //current version is using queueList
    //Make sure to call this method to get the queues before launching the queues in chunkProcessor.
    public static List<LinkedBlockingDeque<String>> addToQueueList() {
        for (int i = 0; i < QUEUES_NUMBER; i++) {
            QUEUE_LIST.add(new LinkedBlockingDeque<>());
        }
        return QUEUE_LIST;
    }


    public static void setChunksFileMappingQueue(String fileName) {
        chunksFileMappingQueue.add(fileName);
    }

    public static int getChunksFileMappingQueueSize() {
        return chunksFileMappingQueue.size();
    }
}