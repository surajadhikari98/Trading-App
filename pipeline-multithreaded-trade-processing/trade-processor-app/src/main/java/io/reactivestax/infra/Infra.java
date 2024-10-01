package io.reactivestax.infra;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class Infra {

    private List<LinkedBlockingDeque<String>> queues = new ArrayList<>();
    private static Map<String, LinkedBlockingDeque<String>> queueMap = new HashMap<>();

    public static Integer readFromApplicationProperties(String propertyName) throws FileNotFoundException {
        Properties properties = new Properties();
        String filePath = "application.properties";
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            properties.load(fileInputStream);
           return Integer.parseInt(properties.getProperty(propertyName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //number of queue begin with 0;
    //Make sure to call this method to get the queues before launching the queues in chunkProcessor.
    public static Map<String, LinkedBlockingDeque<String>> addToQueueMap() throws FileNotFoundException {
        Integer queuesNumber = readFromApplicationProperties("queues.number");
        for (int i = 0; i <queuesNumber ; i++) {
            String name = "queues" + i;
            queueMap.put(name, new LinkedBlockingDeque<>());
        }
        return queueMap;
    }
}
