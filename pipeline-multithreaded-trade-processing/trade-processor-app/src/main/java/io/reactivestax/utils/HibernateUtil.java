package io.reactivestax.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.reactivestax.entity.TradePayload;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import javax.sql.DataSource;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    private static SessionFactory buildSessionFactory() {
        try {
            // HikariCP configuration
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl("jdbc:mysql://localhost:3306/bootcamp");
            hikariConfig.setUsername("host");
            hikariConfig.setPassword("password123");

            // HikariCP settings (optional - tune according to your needs)
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(5);
            hikariConfig.setIdleTimeout(30000);
            hikariConfig.setConnectionTimeout(20000);
            hikariConfig.setPoolName("HibernateHikariCP");

            DataSource dataSource = new HikariDataSource(hikariConfig);

            // Hibernate configuration
            Configuration configuration = new Configuration();

            // Register entity classes
            configuration.addAnnotatedClass(TradePayload.class);
            // Add more entities as needed

            // Set Hibernate properties
            configuration.setProperty("hibernate.show_sql", "true");
            configuration.setProperty("hibernate.format_sql", "true");
            configuration.setProperty("hibernate.hbm2ddl.auto", "update"); // Use 'validate' in production

            // Set the datasource
            configuration.getProperties().put("hibernate.connection.datasource", dataSource);

            // Build the ServiceRegistry
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            // Build the SessionFactory
            return configuration.buildSessionFactory(serviceRegistry);

        } catch (Exception ex) {
            throw new ExceptionInInitializerError("Initial SessionFactory creation failed: " + ex);
        }
    }

    // Thread-safe singleton access to SessionFactory
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (HibernateUtil.class) {
                if (sessionFactory == null) {
                    sessionFactory = buildSessionFactory();
                }
            }
        }
        return sessionFactory;
    }

    // Shutdown the SessionFactory (optional)
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}