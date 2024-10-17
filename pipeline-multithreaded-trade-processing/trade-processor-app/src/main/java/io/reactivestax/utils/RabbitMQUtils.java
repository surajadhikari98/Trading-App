package io.reactivestax.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Getter
@Slf4j
public class RabbitMQUtils {


    // Single connection setup
    private static RabbitMQUtils instance;

    private Connection connection;


    private RabbitMQUtils() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setUsername("guest");
            factory.setPassword("guest");
            connection = factory.newConnection();
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (TimeoutException e) {
            log.error(e.getMessage());
        }
    }


    public static RabbitMQUtils getInstance() {
        if (instance == null) {
            synchronized (RabbitMQUtils.class) {
                if (instance == null) {
                    instance = new RabbitMQUtils();
                }
            }
        }
        return instance;
    }

    // For each thread, create a new channel from the shared connection
    public Channel createChannel() throws IOException {
        return connection.createChannel();
    }

    public void shutdown() {
        if (connection != null && connection.isOpen()) {
            connection.abort();
        }
    }
}
