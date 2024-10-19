package io.reactivestax.factory;

import io.reactivestax.contract.MessageSender;
import io.reactivestax.contract.repository.PayloadRepository;
import io.reactivestax.exception.InvalidPersistenceTechnologyException;
import io.reactivestax.message.sender.InMemoryQueueMessageSender;
import io.reactivestax.message.sender.RabbitMQMessageSender;
import io.reactivestax.repository.jdbc.JDBCTradePayloadRepository;
import io.reactivestax.repository.hibernate.HibernateTradePayloadRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;

import static io.reactivestax.infra.Infra.readFromApplicationPropertiesStringFormat;

@Slf4j
public class BeanFactory {

    private BeanFactory(){}

    private static final String RABBITMQ_MESSAGING_TECHNOLOGY = "rabbitmq";
    private static final String INMEMORY_MESSAGING_TECHNOLOGY = "inmemory";

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
        } else if(INMEMORY_MESSAGING_TECHNOLOGY.equals(messagingTechnology)){
            return InMemoryQueueMessageSender.getInstance();
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

//    public static LookupSecuritiesRepository getLookupSecuritiesRepository() {
//        ApplicationPropertiesUtils applicationPropertiesUtils = ApplicationPropertiesUtils.getInstance();
//        if(HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(applicationPropertiesUtils.getPersistenceTechnology())){
//            return HibernateSecuritiesReferenceRepository.getInstance();
//        } else if(JDBC_PERSISTENCE_TECHNOLOGY.equals(applicationPropertiesUtils.getPersistenceTechnology())){
//            return JDBCSecuritiesReferenceRepository.getInstance();
//        } else{
//            throw new InvalidPersistenceTechnologyException("Invalid persistence technology");
//        }
//    }
//
    public static JournalEntryRepository getJournalEntryRepository() {
        if(HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)){
            return HibernateJournalEntryRepository.getInstance();
        } else if(JDBC_PERSISTENCE_TECHNOLOGY.equals(MESSAGING_TECHNOLOGY)){
            return JDBCJournalEntryRepository.getInstance();
        } else{
            throw new InvalidPersistenceTechnologyException("Invalid persistence technology");
        }
    }
//
//    public static PositionsRepository getPositionsRepository() {
//        ApplicationPropertiesUtils applicationPropertiesUtils = ApplicationPropertiesUtils.getInstance();
//        if(HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(applicationPropertiesUtils.getPersistenceTechnology())){
//            return HibernatePositionsRepository.getInstance();
//        } else if(JDBC_PERSISTENCE_TECHNOLOGY.equals(applicationPropertiesUtils.getPersistenceTechnology())){
//            return JDBCPositionsRepository.getInstance();
//        } else{
//            throw new InvalidPersistenceTechnologyException("Invalid persistence technology");
//        }
//    }
//
//    public static TransactionUtil getTransactionUtil() {
//        ApplicationPropertiesUtils applicationPropertiesUtils = ApplicationPropertiesUtils.getInstance();
//        if(HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(applicationPropertiesUtils.getPersistenceTechnology())){
//            return HibernateTransactionUtil.getInstance();
//        } else if(JDBC_PERSISTENCE_TECHNOLOGY.equals(applicationPropertiesUtils.getPersistenceTechnology())){
//            return JDBCTransactionUtil.getInstance();
//        } else{
//            throw new InvalidPersistenceTechnologyException("Invalid persistence technology");
//        }
//    }

}
