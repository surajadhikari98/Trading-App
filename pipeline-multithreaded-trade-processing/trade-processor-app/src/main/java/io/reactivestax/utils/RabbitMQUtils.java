package io.reactivestax.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQUtils {


    // Single connection setup
    private static Connection connection;

    public static Connection getConnection() throws IOException, TimeoutException {
        if (connection == null || !connection.isOpen()) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setUsername("guest");
            factory.setPassword("guest");
            connection = factory.newConnection();
        }
        return connection;
    }

    // For each thread, create a new channel from the shared connection
    public static Channel createChannel() throws IOException, TimeoutException {
        return getConnection().createChannel();
    }
}
