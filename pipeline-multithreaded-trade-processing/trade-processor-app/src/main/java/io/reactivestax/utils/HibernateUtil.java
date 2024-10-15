package io.reactivestax.utils;

import io.reactivestax.entity.JournalEntries;
import io.reactivestax.entity.TradePayload;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@Getter
@Slf4j
public class HibernateUtil {
    private static volatile HibernateUtil instance;

    private final SessionFactory sessionFactory;

    private HibernateUtil() {
        try {
            Configuration configuration = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(TradePayload.class)
                    .addAnnotatedClass(JournalEntries.class);
            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed");
            throw new ExceptionInInitializerError(ex);
        }
    }

     //Returns the singleton instance of HibernateUtil.
    public static HibernateUtil getInstance() {
        if (instance == null) {
            synchronized (HibernateUtil.class) {
                if (instance == null) {
                    instance = new HibernateUtil();
                }
            }
        }
        return instance;
    }


    public Session getSession() {
        return sessionFactory.openSession();
    }

    /**
     * Closes the SessionFactory. Should be called during application shutdown.
     */
    public void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
