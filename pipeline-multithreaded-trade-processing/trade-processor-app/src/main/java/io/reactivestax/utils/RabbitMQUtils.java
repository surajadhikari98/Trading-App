package io.reactivestax.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RabbitMQUtils {


    // Single connection setup
    private static Connection connection;

    private static RabbitMQUtils instance;

    private RabbitMQUtils() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");
        connection = factory.newConnection();
    }

    public static RabbitMQUtils getInstance() throws IOException, TimeoutException {
        if (instance == null) {
            synchronized (RabbitMQUtils.class) {
                    instance = new RabbitMQUtils();
            }
        }
        return instance;
    }

    public Channel getChannel() throws IOException {
        return connection.createChannel();
    }

}
