package io.reactivestax.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQUtils {

    private static Channel channel;

    public static Channel setUpConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // Or the RabbitMQ server IP/hostname
        factory.setUsername("guest"); // RabbitMQ username
        factory.setPassword("guest"); // RabbitMQ password

        // Establish connection and create channel
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            return channel;
        }
    }

    // Thread-safe singleton access to SessionFactory
    public static Channel getChannel() throws IOException, TimeoutException {
        if (channel == null) {
            synchronized (HibernateUtil.class) {
                    channel = setUpConnection();
            }
        }
        return channel;
    }
}
