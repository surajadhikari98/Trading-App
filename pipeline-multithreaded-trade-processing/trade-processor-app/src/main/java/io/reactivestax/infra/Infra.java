package io.reactivestax.infra;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class Infra {

    private static final List<LinkedBlockingDeque<String>> QUEUE_LIST = new ArrayList<>();
    private static final Map<String, LinkedBlockingDeque<String>> QUEUE_MAP = new HashMap<>();
    private static final LinkedBlockingQueue<String> chunksFileMappingQueue = new LinkedBlockingQueue<>();
     private static final Integer QUEUES_NUMBER;

    static {
        QUEUES_NUMBER = readFromApplicationPropertiesIntegerFormat("numberOfQueues");
    }

    public static String readFromApplicationPropertiesStringFormat(String propertyName) throws FileNotFoundException {
        Properties properties = new Properties();
        String propName = "";
//        String filePath = "src/main/resources/application.properties";
        String filePath = "C:\\Users\\suraj\\source\\full-stack-student-mode\\suad-bootcamp-2024\\pipeline-multithreaded-trade-processing\\trade-processor-app\\src\\main\\resources\\application.properties";

//        String filePath = "src/test/resources/application.properties";
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            properties.load(fileInputStream);
            return properties.getProperty(propertyName);
        } catch (IOException e) {
            System.out.println("application-reading" + e.getMessage());
        }
        return propName;
    }

    public static int readFromApplicationPropertiesIntegerFormat(String propertyName)  {
        Properties properties = new Properties();
        int propName = 0;
//        String filePath = "src/main/resources/application.properties";
        String filePath = "C:\\Users\\suraj\\source\\full-stack-student-mode\\suad-bootcamp-2024\\pipeline-multithreaded-trade-processing\\trade-processor-app\\src\\main\\resources\\application.properties";

        //for Test environment
//        String filePath = "src/test/resources/application.properties";
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            properties.load(fileInputStream);
            return Integer.parseInt(properties.getProperty(propertyName));
        } catch (IOException e) {
            System.out.println("application-reading" + e.getMessage());
        }
        return propName;
    }

    //current version is using queueList
    public static List<LinkedBlockingDeque<String>> addToQueueList() {
        for (int i = 0; i < QUEUES_NUMBER; i++) {
            QUEUE_LIST.add(new LinkedBlockingDeque<>());
        }
        return QUEUE_LIST;
    }

    //number of queue begin with 0;
    //Make sure to call this method to get the queues before launching the queues in chunkProcessor.
    public static Map<String, LinkedBlockingDeque<String>> addToQueueMap() {
        for (int i = 0; i < QUEUES_NUMBER; i++) {
            String name = "queue" + i;
            QUEUE_MAP.put(name, new LinkedBlockingDeque<>());
        }
        return QUEUE_MAP;
    }


    public static LinkedBlockingQueue<String> getChunksFileMappingQueue() {
        return chunksFileMappingQueue;
    }

    public static void setChunksFileMappingQueue(String fileName) {
        Infra.chunksFileMappingQueue.add(fileName);
    }

    public static int getChunksFileMappingQueueSize(){
       return Infra.chunksFileMappingQueue.size();
    }
}
