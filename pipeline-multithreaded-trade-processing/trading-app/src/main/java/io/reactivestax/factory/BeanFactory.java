package io.reactivestax.factory;

import io.reactivestax.contract.MessageSender;
import io.reactivestax.contract.QueueSetup;
import io.reactivestax.contract.repository.*;
import io.reactivestax.exception.InvalidPersistenceTechnologyException;
import io.reactivestax.message.sender.InMemoryQueueMessageSender;
import io.reactivestax.message.sender.RabbitMQMessageSender;
import io.reactivestax.message.sender.RabbitMQSetup;
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
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;

import static io.reactivestax.infra.Infra.readFromApplicationPropertiesStringFormat;

@Slf4j
public class BeanFactory {

    private BeanFactory(){}

    private static final String RABBITMQ_MESSAGING_TECHNOLOGY = "rabbitmq";
    private static final String IN_MEMORY_MESSAGING_TECHNOLOGY = "inmemory";

    private static final String HIBERNATE_PERSISTENCE_TECHNOLOGY = "hibernate";
    private static final String JDBC_PERSISTENCE_TECHNOLOGY = "jdbc";

    private static  final String MESSAGING_TECHNOLOGY;

    static {
        try {
            MESSAGING_TECHNOLOGY = readFromApplicationPropertiesStringFormat("persistence.technology");
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public static MessageSender getQueueMessageSender() throws FileNotFoundException {
        String messagingTechnology = readFromApplicationPropertiesStringFormat("messaging.technology");
        if(RABBITMQ_MESSAGING_TECHNOLOGY.equals(messagingTechnology)){
            return RabbitMQMessageSender.getInstance();
        } else if(IN_MEMORY_MESSAGING_TECHNOLOGY.equals(messagingTechnology)){
            return InMemoryQueueMessageSender.getInstance();
        } else{
            throw new InvalidPersistenceTechnologyException("Invalid messaging technology");
        }
    }

    public static QueueSetup getQueueSetUp() throws FileNotFoundException {
        String messagingTechnology = readFromApplicationPropertiesStringFormat("messaging.technology");
        if(RABBITMQ_MESSAGING_TECHNOLOGY.equals(messagingTechnology)){
            return RabbitMQSetup.getInstance();
        } else if(IN_MEMORY_MESSAGING_TECHNOLOGY.equals(messagingTechnology)){
            return null;
        } else{
            throw new InvalidPersistenceTechnologyException("Invalid messaging technology");
        }
    }



    public static PayloadRepository getTradePayloadRepository() throws FileNotFoundException {
        if(HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)){
            return HibernateTradePayloadRepository.getInstance();
        } else if(JDBC_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)){
            return JDBCTradePayloadRepository.getInstance();
        } else{
            throw new InvalidPersistenceTechnologyException("Invalid persistence technology");
        }
    }

    public static SecuritiesReferenceRepository getLookupSecuritiesRepository() {
        if(HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)){
            return HibernateSecuritiesReferenceRepository.getInstance();
        } else if(JDBC_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)){
            return JDBCSecuritiesReferenceRepository.getInstance();
        } else{
            throw new InvalidPersistenceTechnologyException("Invalid persistence technology");
        }
    }

    public static JournalEntryRepository getJournalEntryRepository() {
        if(HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)){
            return HibernateJournalEntryRepository.getInstance();
        } else if(JDBC_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)){
            return JDBCJournalEntryRepository.getInstance();
        } else{
            throw new InvalidPersistenceTechnologyException("Invalid persistence technology");
        }
    }

    public static PositionRepository getPositionsRepository() {
        if(HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)){
            return HibernateTradePositionRepository.getInstance();
        } else if(JDBC_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)){
            return JDBCTradePositionRepository.getInstance();
        } else{
            throw new InvalidPersistenceTechnologyException("Invalid persistence technology");
        }
    }


    public static TransactionUtil getTransactionUtil() {
        if(HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)){
            return HibernateUtil.getInstance();
        } else if(JDBC_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)){
            return DBUtils.getInstance();
        } else{
            throw new InvalidPersistenceTechnologyException("Invalid persistence technology");
        }
    }

}
