package io.reactivestax.utility.messaging;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import io.reactivestax.types.contract.QueueLoader;
import io.reactivestax.types.enums.RabbitMQHeaders;
import io.reactivestax.utility.messaging.reciever.dlq.CustomCancelCallback;
import io.reactivestax.utility.messaging.reciever.dlq.RabbitMQMessageCallBack;
import io.reactivestax.utility.RabbitMQUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static io.reactivestax.factory.BeanFactory.readFromApplicationPropertiesStringFormat;

public class RabbitMQLoader implements QueueLoader {

    private static RabbitMQLoader instance;

    private RabbitMQLoader() {
    }

    public static synchronized RabbitMQLoader getInstance() {
        if (instance == null) {
            instance = new RabbitMQLoader();
        }
        return instance;
    }

    public void consumeMessage(String queueName) throws IOException, TimeoutException {

        Channel channel = setUpQueueWithRetry(queueName);

        DeliverCallback deliverCallback = new RabbitMQMessageCallBack(channel, queueName);

        CancelCallback cancelCallback = new CustomCancelCallback();

        // Start consuming messages with manual acknowledgment
        channel.basicConsume(queueName, false, deliverCallback, cancelCallback);
    }

    private static Channel setUpQueueWithRetry(String queueName) throws IOException, TimeoutException {
        String dlxExchange = readFromApplicationPropertiesStringFormat("queue.dlx.exchange");
        Channel channel = RabbitMQUtils.getRabbitMQChannel();

        channel.exchangeDeclare(dlxExchange, "direct");

        // DLQ should have a TTL and dead-lettering back to the main queue
        Map<String, Object> dlqArguments = new HashMap<>();
        dlqArguments.put(RabbitMQHeaders.X_TTL.getHeaderKey(), 5000);
        dlqArguments.put(RabbitMQHeaders.X_DLE.getHeaderKey(),
                readFromApplicationPropertiesStringFormat("queue.exchange.name")); // Requeue to main exchange
        dlqArguments.put(RabbitMQHeaders.X_DLRK.getHeaderKey() + queueName, queueName); // Requeue to the main queue, here the main key routing key is queueName.

        // Declare DLQ with TTL
        channel.queueDeclare(readFromApplicationPropertiesStringFormat("queue.dlx") + queueName, true, false, false, dlqArguments);
        channel.queueBind(readFromApplicationPropertiesStringFormat("queue.dlx") + queueName, dlxExchange, queueName);

        // Declare the main Quorum Queue with DLX routing
        Map<String, Object> mainQueueArguments = new HashMap<>();
        mainQueueArguments.put(RabbitMQHeaders.X_QT.getHeaderKey(), "quorum"); // Declare quorum queue
        mainQueueArguments.put(RabbitMQHeaders.X_DLE.getHeaderKey(), dlxExchange); // If a message is rejected, send to DLX
        mainQueueArguments.put(RabbitMQHeaders.X_DLRK.getHeaderKey(), queueName);

        channel.queueDeclare(queueName, true, false, false, mainQueueArguments); //Declaring the main queue with the argument


        //dead-queue to hold the rejected trade after reaching max retries.
        channel.queueBind(queueName, readFromApplicationPropertiesStringFormat("queue.exchange.name"), queueName);
        channel.exchangeDeclare(RabbitMQHeaders.X_DLE.getHeaderKey(), "direct");
        channel.queueDeclare(RabbitMQHeaders.X_DEATH.getHeaderKey(), true, false, false, null);
        channel.queueBind(RabbitMQHeaders.X_DEATH.getHeaderKey(), RabbitMQHeaders.X_DLE.getHeaderKey(), "dead-routing-key");
        return channel;
    }
}
