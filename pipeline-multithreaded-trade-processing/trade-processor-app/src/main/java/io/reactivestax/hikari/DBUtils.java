package io.reactivestax.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;


public class DBUtils {

    private static DBUtils instance;

    private HikariDataSource dataSource;

    private DBUtils() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/bootcamp");
        config.setUsername("root");
        config.setPassword("password123");
        config.setMaximumPoolSize(100);

        // Optional HikariCP settings
        config.setMaximumPoolSize(50); // Max 10 connections in the pool
        config.setMinimumIdle(5); // Minimum idle connections
        config.setConnectionTimeout(30000); // 30 seconds timeout for obtaining a connection
        config.setIdleTimeout(600000); // 10 minutes idle timeout

        dataSource = new HikariDataSource(config);
        System.out.println("Total Connections: " + dataSource.getHikariPoolMXBean().getTotalConnections());
        System.out.println("Active Connections: " + dataSource.getHikariPoolMXBean().getActiveConnections());
        System.out.println("Idle Connections: " + dataSource.getHikariPoolMXBean().getIdleConnections());
        System.out.println("Waiting Threads: " + dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }

    public static synchronized DBUtils getInstance() {
            if (instance == null) {
                instance = new DBUtils();
            }
            return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}