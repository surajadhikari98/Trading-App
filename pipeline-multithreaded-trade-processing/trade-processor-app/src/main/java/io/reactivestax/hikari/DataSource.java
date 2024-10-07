package io.reactivestax.hikari;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariConfig;
import io.reactivestax.component.TradeCsvChunkGenerator;
import io.reactivestax.infra.Infra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {
    private static final Logger logger = LoggerFactory.getLogger(DataSource.class);


    private static HikariDataSource dataSource;

    static {
        // Configure the HikariCP connection pool
        HikariConfig config = new HikariConfig();
        try {
            config.setJdbcUrl(Infra.readFromApplicationPropertiesStringFormat("dbUrl"));
            config.setUsername(Infra.readFromApplicationPropertiesStringFormat("dbUserName"));
            config.setPassword(Infra.readFromApplicationPropertiesStringFormat("dbPassword"));
        } catch (FileNotFoundException e) {
            logger.error("File is not found: {}", e.getMessage());
        }


        // Optional HikariCP settings
        config.setMaximumPoolSize(50); // Max 10 connections in the pool
        config.setMinimumIdle(5); // Minimum idle connections
        config.setConnectionTimeout(30000); // 30 seconds timeout for obtaining a connection
        config.setIdleTimeout(600000); // 10 minutes idle timeout

        // Create the HikariCP data source
        dataSource = new HikariDataSource(config);
    }

    private DataSource() throws FileNotFoundException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Infra.readFromApplicationPropertiesStringFormat("dbUrl"));
        config.setUsername(Infra.readFromApplicationPropertiesStringFormat("dbUserName"));
        config.setPassword(Infra.readFromApplicationPropertiesStringFormat("dbPassword"));

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


    public static Connection getConnection(){
        // Fetch a connection from the pool
        Connection connection = null;
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("Sql error in connection : {}", e.getMessage());
        }
        return connection;
    }

    public static void close() {
        // Close the data source (usually when the app shuts down)
        if (dataSource != null) {
            dataSource.close();
        }
    }

}
