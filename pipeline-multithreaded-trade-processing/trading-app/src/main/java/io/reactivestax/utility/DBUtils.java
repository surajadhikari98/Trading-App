package io.reactivestax.utility;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.reactivestax.contract.repository.ConnectionUtil;
import io.reactivestax.contract.repository.TransactionUtil;
import io.reactivestax.exception.HikariCPConnectionException;
import io.reactivestax.exception.TransactionHandlingException;
import io.reactivestax.factory.BeanFactory;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;


@Slf4j
public class DBUtils implements TransactionUtil, ConnectionUtil<Connection> {

    private static DBUtils instance;
    private DataSource dataSource;
    private final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    private DBUtils() {
    }

    private void createDataSource() throws FileNotFoundException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(BeanFactory.readFromApplicationPropertiesStringFormat("db.url"));
        config.setUsername(BeanFactory.readFromApplicationPropertiesStringFormat("db.user.name"));
        config.setPassword(BeanFactory.readFromApplicationPropertiesStringFormat("db.password"));

        // Optional HikariCP settings
        config.setMaximumPoolSize(50); // Max 50 connections in the pool
        config.setMinimumIdle(5); // Minimum idle connections
        config.setConnectionTimeout(30000); // 30 seconds timeout for obtaining a connection
        config.setIdleTimeout(600000); // 10 minutes idle timeout

        // Create the HikariCP data source
        dataSource = new HikariDataSource(config);
    }


    public static synchronized DBUtils getInstance() {
        if (instance == null) {
            instance = new DBUtils();
        }
        return instance;
    }

    public Connection getConnection() throws FileNotFoundException {
        Connection connection = connectionHolder.get();
        if (connection == null) {
            dataSource = getHikkariDataSource();
            try {
                connection = dataSource.getConnection();
                connectionHolder.set(connection);
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new HikariCPConnectionException("Error getting connection from HikkariCp", e);
            }
        }

        return connection;
    }

    private synchronized DataSource getHikkariDataSource() throws FileNotFoundException {
        if (dataSource == null) {
            createDataSource();
        }
        return dataSource;
    }

    private void closeConnection() {
        Connection connection = connectionHolder.get();
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                log.error(e.getMessage());
            } finally {
                connectionHolder.remove();
            }
        }
    }

    @Override
    public void startTransaction() {
        try {
            getConnection().setAutoCommit(false);
        } catch (SQLException | FileNotFoundException e) {
            log.error(e.getMessage());
        }
    }

    public void commitTransaction() {
        try {
            connectionHolder.get().commit();
            connectionHolder.get().setAutoCommit(false);
            closeConnection();
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new TransactionHandlingException("error committing transaction", e);
        }
    }

    public void rollbackTransaction() {
        try {
            connectionHolder.get().rollback();
            connectionHolder.get().setAutoCommit(false);
            closeConnection();
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new TransactionHandlingException("error rolling back transaction", e);
        }
    }
}