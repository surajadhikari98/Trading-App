package io.reactivestax.utility;

import io.reactivestax.types.contract.repository.ConnectionUtil;
import io.reactivestax.types.contract.repository.TransactionUtil;
import io.reactivestax.repository.hibernate.entity.JournalEntries;
import io.reactivestax.repository.hibernate.entity.Position;
import io.reactivestax.repository.hibernate.entity.SecuritiesReference;
import io.reactivestax.repository.hibernate.entity.TradePayload;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

@Getter
@Slf4j
public class HibernateUtil implements TransactionUtil, ConnectionUtil<Session> {
    private static volatile HibernateUtil instance;
    private static final ThreadLocal<Session> threadLocalSession = new ThreadLocal<>();

    private static SessionFactory sessionFactory;
    private static final String DEFAULT_RESOURCE = "hibernate.cfg.xml";

    private HibernateUtil() {
    }


    private static synchronized SessionFactory buildSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration()
                        .configure(HibernateUtil.DEFAULT_RESOURCE)
                        .addAnnotatedClass(TradePayload.class)
                        .addAnnotatedClass(Position.class)
                        .addAnnotatedClass(JournalEntries.class)
                        .addAnnotatedClass(SecuritiesReference.class);

                StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();
                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (HibernateException e) {
                log.error("Initial Session Factory creation failed. {}", e);
                throw new ExceptionInInitializerError(e);
            }
        }
        return sessionFactory;
    }

    //Returns the singleton instance of HibernateUtil.
    public static synchronized HibernateUtil getInstance() {
        if (instance == null) {
            instance = new HibernateUtil();
        }
        return instance;
    }

    public void startTransaction() {
        getConnection().beginTransaction();
    }

    @Override
    public Session getConnection() {
        Session session = threadLocalSession.get();
        if (session == null) {
            session = buildSessionFactory().openSession();
            threadLocalSession.set(session);
        }
        return session;
    }

    private void closeConnection() {
        Session session = threadLocalSession.get();
        if (session != null) {
            session.close();
            threadLocalSession.remove();
        }
    }

    public void commitTransaction() {
        getConnection().getTransaction().commit();
        closeConnection();
    }

    public void rollbackTransaction() {
        getConnection().getTransaction().rollback();
        closeConnection();
    }

}
