package io.reactivestax.factory;

import io.reactivestax.contract.MessageSender;
import io.reactivestax.contract.QueueSetup;
import io.reactivestax.contract.repository.*;
import io.reactivestax.exception.InvalidPersistenceTechnologyException;
import io.reactivestax.message.sender.InMemoryQueueMessageSender;
import io.reactivestax.message.sender.RabbitMQMessageSender;
import io.reactivestax.message.RabbitMQSetup;
import io.reactivestax.repository.hibernate.HibernateJournalEntryRepository;
import io.reactivestax.repository.hibernate.HibernateSecuritiesReferenceRepository;
import io.reactivestax.repository.hibernate.HibernateTradePositionRepository;
import io.reactivestax.repository.jdbc.JDBCJournalEntryRepository;
import io.reactivestax.repository.jdbc.JDBCSecuritiesReferenceRepository;
import io.reactivestax.repository.jdbc.JDBCTradePayloadRepository;
import io.reactivestax.repository.hibernate.HibernateTradePayloadRepository;
import io.reactivestax.repository.jdbc.JDBCTradePositionRepository;
import io.reactivestax.utils.DBUtils;
import io.reactivestax.utils.HibernateUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;


@Slf4j
public class BeanFactory {

    private BeanFactory() {
    }

    @Getter
    private static final LinkedBlockingQueue<String> chunksFileMappingQueue = new LinkedBlockingQueue<>();
    @Getter
    private static final List<LinkedBlockingDeque<String>> QUEUE_LIST = new ArrayList<>();
    private static final Integer QUEUES_NUMBER;


    private static final String RABBITMQ_MESSAGING_TECHNOLOGY = "rabbitmq";
    private static final String IN_MEMORY_MESSAGING_TECHNOLOGY = "inmemory";

    private static final String HIBERNATE_PERSISTENCE_TECHNOLOGY = "hibernate";
    private static final String JDBC_PERSISTENCE_TECHNOLOGY = "jdbc";
    private static final String ERROR_MESSAGE = "Invalid messaging technology";

    private static final String MESSAGING_TECHNOLOGY;

    static {
        try {
            MESSAGING_TECHNOLOGY = readFromApplicationPropertiesStringFormat("persistence.technology");
            QUEUES_NUMBER = readFromApplicationPropertiesIntegerFormat("number.queues");

        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public static MessageSender getQueueMessageSender() throws FileNotFoundException {
        String messagingTechnology = readFromApplicationPropertiesStringFormat("messaging.technology");
        if (RABBITMQ_MESSAGING_TECHNOLOGY.equals(messagingTechnology)) {
            return RabbitMQMessageSender.getInstance();
        } else if (IN_MEMORY_MESSAGING_TECHNOLOGY.equals(messagingTechnology)) {
            return InMemoryQueueMessageSender.getInstance();
        } else {
            throw new InvalidPersistenceTechnologyException(ERROR_MESSAGE);
        }
    }

    public static QueueSetup getQueueSetUp() throws FileNotFoundException {
        String messagingTechnology = readFromApplicationPropertiesStringFormat("messaging.technology");
        if (RABBITMQ_MESSAGING_TECHNOLOGY.equals(messagingTechnology)) {
            return RabbitMQSetup.getInstance();
        } else if (IN_MEMORY_MESSAGING_TECHNOLOGY.equals(messagingTechnology)) {
            return null;
        } else {
            throw new InvalidPersistenceTechnologyException(ERROR_MESSAGE);
        }
    }


    public static PayloadRepository getTradePayloadRepository() {
        if (HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)) {
            return HibernateTradePayloadRepository.getInstance();
        } else if (JDBC_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)) {
            return JDBCTradePayloadRepository.getInstance();
        } else {
            throw new InvalidPersistenceTechnologyException(ERROR_MESSAGE);
        }
    }

    public static SecuritiesReferenceRepository getLookupSecuritiesRepository() {
        if (HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)) {
            return HibernateSecuritiesReferenceRepository.getInstance();
        } else if (JDBC_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)) {
            return JDBCSecuritiesReferenceRepository.getInstance();
        } else {
            throw new InvalidPersistenceTechnologyException(ERROR_MESSAGE);
        }
    }

    public static JournalEntryRepository getJournalEntryRepository() {
        if (HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)) {
            return HibernateJournalEntryRepository.getInstance();
        } else if (JDBC_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)) {
            return JDBCJournalEntryRepository.getInstance();
        } else {
            throw new InvalidPersistenceTechnologyException(ERROR_MESSAGE);
        }
    }

    public static PositionRepository getPositionsRepository() {
        if (HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)) {
            return HibernateTradePositionRepository.getInstance();
        } else if (JDBC_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)) {
            return JDBCTradePositionRepository.getInstance();
        } else {
            throw new InvalidPersistenceTechnologyException(ERROR_MESSAGE);
        }
    }


    public static TransactionUtil getTransactionUtil() {
        if (HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)) {
            return HibernateUtil.getInstance();
        } else if (JDBC_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)) {
            return DBUtils.getInstance();
        } else {
            throw new InvalidPersistenceTechnologyException(ERROR_MESSAGE);
        }
    }

    public static String readFromApplicationPropertiesStringFormat(String propertyName) throws FileNotFoundException {
        Properties properties = new Properties();
        String propName = "";

        try (InputStream inputStream = BeanFactory.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                throw new FileNotFoundException("Property file 'application.properties' not found in the classpath");
            }

            properties.load(inputStream);
            return properties.getProperty(propertyName);
        } catch (IOException e) {
            log.error("Error reading application properties: {}", e.getMessage());
        }

        return propName;
    }


    public static int readFromApplicationPropertiesIntegerFormat(String propertyName) {
        Properties properties = new Properties();

        // Use class loader to load the file from the resources folder
        try (InputStream inputStream = BeanFactory.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                throw new FileNotFoundException("Property file 'application.properties' not found in the classpath");
            }
            properties.load(inputStream);
            return Integer.parseInt(properties.getProperty(propertyName));
        } catch (IOException e) {
            log.error("Error reading application properties: {}", e.getMessage());
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


}
