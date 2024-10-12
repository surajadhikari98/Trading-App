package io.reactivestax.rabbitmq;

import com.rabbitmq.client.*;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class RabbitMQConsumerCallable implements Callable<Void> {

    private final static String EXCHANGE_NAME = "credit_card_transactions";
    private final String queueName;

    // Counter for the number of messages consumed
    private AtomicInteger messageCounter = new AtomicInteger(0);

    public RabbitMQConsumerCallable(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public Void call() throws Exception {
        // Setup connection factory
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");

        // Establish connection and create channel
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            // Declare the exchange and queue, then bind them
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, EXCHANGE_NAME, "cc_partition_1");

            System.out.println(" [*] Waiting for messages in '" + queueName + "'.");

            // Define message handler
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");

                // Increment the message counter each time a message is consumed
                int currentCount = messageCounter.incrementAndGet();
                System.out.println(" [x] Total messages consumed: " + currentCount);

                // Add processing logic here
            };
            CancelCallback cancelCallback = consumerTag -> {
            };
            // Start consuming messages, blocking the thread to keep it alive
            channel.basicConsume(queueName, true, deliverCallback, cancelCallback);

            // Block indefinitely (until shutdown is triggered)
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(1000); // Keep checking every second
            }

        } catch (InterruptedException e) {
            System.out.println("Consumer interrupted, shutting down...");
        }
        return null;
    }
}
