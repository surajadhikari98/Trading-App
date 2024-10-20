package io.reactivestax.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.reactivestax.infra.Infra;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RabbitMQUtils {


        private static final ThreadLocal<Channel> channelThreadLocal = new ThreadLocal<>();

        public static Channel getRabbitMQChannel() throws IOException, TimeoutException {
            if (channelThreadLocal.get() == null) {
                ConnectionFactory connectionFactory = new ConnectionFactory();
                connectionFactory.setHost(Infra.readFromApplicationPropertiesStringFormat("queue.host"));
                connectionFactory.setUsername(Infra.readFromApplicationPropertiesStringFormat("queue.username"));
                connectionFactory.setPassword(Infra.readFromApplicationPropertiesStringFormat("queue.password"));
                Connection connection = connectionFactory.newConnection();
                Channel localChannel = connection.createChannel();
                localChannel.exchangeDeclare(Infra.readFromApplicationPropertiesStringFormat("queue.exchange.name"),
                        Infra.readFromApplicationPropertiesStringFormat("queue.exchange.type"));
                channelThreadLocal.set(localChannel);
            }
            return channelThreadLocal.get();
        }
    }
