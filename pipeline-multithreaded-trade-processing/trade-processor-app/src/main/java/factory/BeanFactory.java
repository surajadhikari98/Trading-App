package factory;

import io.reactivestax.contract.MessageSender;
import io.reactivestax.exception.InvalidPersistenceTechnologyException;
import io.reactivestax.infra.Infra;
import io.reactivestax.rabbitmq.InMemoryQueueMessageSender;
import io.reactivestax.rabbitmq.RabbitMQMessageSender;
import io.reactivestax.repository.TradePayloadRepository;

import java.io.FileNotFoundException;

public class BeanFactory {

    private BeanFactory(){}

    private static final String RABBITMQ_MESSAGING_TECHNOLOGY = "rabbitmq";
    private static final String INMEMORY_MESSAGING_TECHNOLOGY = "inmemory";

    public static MessageSender getQueueMessageSender() throws FileNotFoundException {
        String messagingTechnology = Infra.readFromApplicationPropertiesStringFormat("messaging.technology");
        if(RABBITMQ_MESSAGING_TECHNOLOGY.equals(messagingTechnology)){
            return RabbitMQMessageSender.getInstance();
        } else if(INMEMORY_MESSAGING_TECHNOLOGY.equals(messagingTechnology)){
            return InMemoryQueueMessageSender.getInstance();
        } else{
            throw new InvalidPersistenceTechnologyException("Invalid messaging technology");
        }
    }


    public static TradePayloadRepository getTradePayloadRepository() {
        String messagingTechnology = Infra.readFromApplicationPropertiesStringFormat("messaging.technology");
        if(HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(applicationPropertiesUtils.getPersistenceTechnology())){
            return HibernateTradePayloadRepository.getInstance();
        } else if(JDBC_PERSISTENCE_TECHNOLOGY.equals(applicationPropertiesUtils.getPersistenceTechnology())){
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
//    public static JournalEntryRepository getJournalEntryRepository() {
//        ApplicationPropertiesUtils applicationPropertiesUtils = ApplicationPropertiesUtils.getInstance();
//        if(HIBERNATE_PERSISTENCE_TECHNOLOGY.equals(applicationPropertiesUtils.getPersistenceTechnology())){
//            return HibernateJournalEntryRepository.getInstance();
//        } else if(JDBC_PERSISTENCE_TECHNOLOGY.equals(applicationPropertiesUtils.getPersistenceTechnology())){
//            return JDBCJournalEntryRepository.getInstance();
//        } else{
//            throw new InvalidPersistenceTechnologyException("Invalid persistence technology");
//        }
//    }
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
