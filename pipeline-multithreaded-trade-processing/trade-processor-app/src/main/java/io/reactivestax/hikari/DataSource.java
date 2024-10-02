package io.reactivestax.hikari;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariConfig;
import io.reactivestax.infra.Infra;

import java.io.FileNotFoundException;
import java.sql.Connection;

public class DataSource {

    private static HikariDataSource dataSource;

    static {
        // Configure the HikariCP connection pool
        HikariConfig config = new HikariConfig();
        try {
            config.setJdbcUrl(Infra.readFromApplicationPropertiesStringFormat("dbUrl"));
            config.setUsername(Infra.readFromApplicationPropertiesStringFormat("dbUserName"));
            config.setPassword(Infra.readFromApplicationPropertiesStringFormat("dbPassword"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        // Optional HikariCP settings
        config.setMaximumPoolSize(50); // Max 10 connections in the pool
        config.setMinimumIdle(5); // Minimum idle connections
        config.setConnectionTimeout(30000); // 30 seconds timeout for obtaining a connection
        config.setIdleTimeout(600000); // 10 minutes idle timeout

        // Create the HikariCP data source
        dataSource = new HikariDataSource(config);
    }

    public static HikariDataSource getDataSource() throws Exception {
        // Fetch a connection from the pool
        return dataSource;
    }


    public static Connection getConnection() throws Exception {
        // Fetch a connection from the pool
        return dataSource.getConnection();
    }

    public static void close() {
        // Close the data source (usually when the app shuts down)
        if (dataSource != null) {
            dataSource.close();
        }
    }

}
