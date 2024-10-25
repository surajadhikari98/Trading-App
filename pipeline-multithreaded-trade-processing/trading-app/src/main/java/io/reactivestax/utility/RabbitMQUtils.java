package io.reactivestax.utility;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.reactivestax.factory.BeanFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RabbitMQUtils {


        private static final ThreadLocal<Channel> channelThreadLocal = new ThreadLocal<>();

        public static Channel getRabbitMQChannel() throws IOException, TimeoutException {
            if (channelThreadLocal.get() == null) {
                ConnectionFactory connectionFactory = new ConnectionFactory();
                connectionFactory.setHost(BeanFactory.readFromApplicationPropertiesStringFormat("queue.host"));
                connectionFactory.setUsername(BeanFactory.readFromApplicationPropertiesStringFormat("queue.username"));
                connectionFactory.setPassword(BeanFactory.readFromApplicationPropertiesStringFormat("queue.password"));
                Connection connection = connectionFactory.newConnection();
                Channel localChannel = connection.createChannel();
                localChannel.exchangeDeclare(BeanFactory.readFromApplicationPropertiesStringFormat("queue.exchange.name"),
                        BeanFactory.readFromApplicationPropertiesStringFormat("queue.exchange.type"));
                channelThreadLocal.set(localChannel);
            }
            return channelThreadLocal.get();
        }
    }
