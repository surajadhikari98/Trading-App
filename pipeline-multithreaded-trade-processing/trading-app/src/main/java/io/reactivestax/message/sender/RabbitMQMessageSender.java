package io.reactivestax.message.sender;

import io.reactivestax.contract.MessageSender;
import io.reactivestax.utils.RabbitMQUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import static io.reactivestax.infra.Infra.readFromApplicationPropertiesStringFormat;

@Slf4j
public class RabbitMQMessageSender implements MessageSender {

    private static RabbitMQMessageSender instance;

    public static synchronized RabbitMQMessageSender getInstance() {
        if (instance == null) {
            instance = new RabbitMQMessageSender();
        }
        return instance;
    }

    @Override
    public Boolean sendMessageToQueue(String queueName, String message) throws IOException, TimeoutException {

        RabbitMQUtils.getRabbitMQChannel().basicPublish(
                readFromApplicationPropertiesStringFormat("queue.exchange.name"),
                queueName,
                null,
                message.getBytes(StandardCharsets.UTF_8)
        );
        System.out.println(" [x] Sent '" + message + "' with routing key '" + queueName + "'");
        return true;
    }
}
