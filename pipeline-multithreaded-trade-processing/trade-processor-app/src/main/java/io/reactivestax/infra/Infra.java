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
     private static final String queuesNumber;

    static {
        try {
            queuesNumber = readFromApplicationProperties("numberOfQueues");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFromApplicationProperties(String propertyName) throws FileNotFoundException {
        Properties properties = new Properties();
        String filePath = "application.properties";
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            properties.load(fileInputStream);
            return properties.getProperty(propertyName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //number of queue begin with 0;
    //Make sure to call this method to get the queues before launching the queues in chunkProcessor.
    public static Map<String, LinkedBlockingDeque<String>> addToQueueMap() {
        for (int i = 0; i < Integer.parseInt(queuesNumber); i++) {
            String name = "queue" + i;
            queueMap.put(name, new LinkedBlockingDeque<>());
        }
        return queueMap;
    }

    public static List<LinkedBlockingDeque<String>> addToQueueList() {
        for (int i = 0; i < Integer.parseInt(queuesNumber); i++) {
            queueList.add(new LinkedBlockingDeque<>());
        }
        return queueList;
    }

    public static LinkedBlockingQueue<String> getChunksFileMappingQueue() {
        return chunksFileMappingQueue;
    }

    public static void setChunksFileMappingQueue(String fileName) {
        Infra.chunksFileMappingQueue.add(fileName);
    }
}
