package io.reactivestax.infra;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class Infra {

    private static List<LinkedBlockingDeque<String>> queueList = new ArrayList<>();
    private static final Map<String, LinkedBlockingDeque<String>> queueMap = new HashMap<>();
    private static final LinkedBlockingQueue<String> chunksFileMappingQueue = new LinkedBlockingQueue<>();
     private static final Integer queuesNumber;

    static {
        try {
            queuesNumber = readFromApplicationPropertiesIntegerFormat("numberOfQueues");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFromApplicationPropertiesStringFormat(String propertyName) throws FileNotFoundException {
        Properties properties = new Properties();
        String filePath = "/Users/Suraj.Adhikari/sources/student-mode-programs/suad-bootcamp-2024/pipeline-multithreaded-trade-processing/trade-processor-app/application.properties";
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            properties.load(fileInputStream);
            return properties.getProperty(propertyName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int readFromApplicationPropertiesIntegerFormat(String propertyName) throws FileNotFoundException {
        Properties properties = new Properties();
        String filePath = "/Users/Suraj.Adhikari/sources/student-mode-programs/suad-bootcamp-2024/pipeline-multithreaded-trade-processing/trade-processor-app/application.properties";
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            properties.load(fileInputStream);
            return Integer.parseInt(properties.getProperty(propertyName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //current version is using queueList
    public static List<LinkedBlockingDeque<String>> addToQueueList() {
        for (int i = 0; i < queuesNumber; i++) {
            queueList.add(new LinkedBlockingDeque<>());
        }
        return queueList;
    }

    //number of queue begin with 0;
    //Make sure to call this method to get the queues before launching the queues in chunkProcessor.
    public static Map<String, LinkedBlockingDeque<String>> addToQueueMap() {
        for (int i = 0; i < queuesNumber; i++) {
            String name = "queue" + i;
            queueMap.put(name, new LinkedBlockingDeque<>());
        }
        return queueMap;
    }


    public static LinkedBlockingQueue<String> getChunksFileMappingQueue() {
        return chunksFileMappingQueue;
    }

    public static void setChunksFileMappingQueue(String fileName) {
        Infra.chunksFileMappingQueue.add(fileName);
    }
}
