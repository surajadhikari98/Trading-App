package io.reactivestax.service.dlq;

import com.rabbitmq.client.*;
import io.reactivestax.utils.RabbitMQUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static io.reactivestax.infra.Infra.readFromApplicationPropertiesIntegerFormat;
import static io.reactivestax.infra.Infra.readFromApplicationPropertiesStringFormat;

@Slf4j
public class RabbitMQConsumerCallable implements Callable<Void> {

    private static final String DLX_QUEUE;
    private static final String MAIN_EXCHANGE;
    private static final String DLX_EXCHANGE;
    private static final int MAX_RETRIES;

    static {
        try {
            DLX_QUEUE = readFromApplicationPropertiesStringFormat("queue.dlx");
            MAIN_EXCHANGE = readFromApplicationPropertiesStringFormat("queue.exchange.name");
            DLX_EXCHANGE = readFromApplicationPropertiesStringFormat("queue.dlx.exchange");
            MAX_RETRIES = readFromApplicationPropertiesIntegerFormat("max.retry.count");
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    // Counter for the number of messages consumed
    private final AtomicInteger messageCounter = new AtomicInteger(0);

    private final String queueName;

    public RabbitMQConsumerCallable(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public Void call() throws Exception {
//        try {
        Channel channel = RabbitMQUtils.getRabbitMQChannel();
        // Declare Dead Letter Exchange (DLX) and Queue (DLQ)
        channel.exchangeDeclare(DLX_EXCHANGE, "direct");

        // DLQ should have a TTL and dead-lettering back to the main queue
        Map<String, Object> dlqArguments = new HashMap<>();
        dlqArguments.put("x-message-ttl", 5000); // Retry delay in milliseconds (5 seconds)
        dlqArguments.put("x-dead-letter-exchange", MAIN_EXCHANGE); // Requeue to main exchange
        dlqArguments.put("x-dead-letter-routing-key" + queueName, queueName); // Requeue to the main queue, here the main key routing key is queueName.

        // Declare DLQ with TTL
        channel.queueDeclare(DLX_QUEUE + queueName, true, false, false, dlqArguments);
        channel.queueBind(DLX_QUEUE + queueName, DLX_EXCHANGE, queueName);

        // Declare the main Quorum Queue with DLX routing
        Map<String, Object> mainQueueArguments = new HashMap<>();
        mainQueueArguments.put("x-queue-type", "quorum"); // Declare quorum queue
        mainQueueArguments.put("x-dead-letter-exchange", DLX_EXCHANGE); // If a message is rejected, send to DLX
        mainQueueArguments.put("x-dead-letter-routing-key", queueName);

        channel.queueDeclare(queueName, true, false, false, mainQueueArguments); //Declaring the main queue with the argument


        //dead-queue to hold the rejected trade after reaching max retries.
        channel.queueBind(queueName, MAIN_EXCHANGE, queueName);
        channel.exchangeDeclare("dead-letter-exchange", "direct");
        channel.queueDeclare("dead-queue", true, false, false, null);
        channel.queueBind("dead-queue", "dead-letter-exchange", "dead-routing-key");


       log.info(" [*] Waiting for messages in {}'" , queueName);

        DeliverCallback deliverCallback = new CustomDeliverCallback(channel, queueName);

        CancelCallback cancelCallback = new CustomCancelCallback();

        // Start consuming messages with manual acknowledgment
        channel.basicConsume(queueName, false, deliverCallback, cancelCallback);
        return null;
    }
}

