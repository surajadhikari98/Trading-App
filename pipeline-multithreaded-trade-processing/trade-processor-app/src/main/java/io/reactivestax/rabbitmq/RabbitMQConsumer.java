package io.reactivestax.rabbitmq;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import io.reactivestax.infra.Infra;
import io.reactivestax.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;



public class RabbitMQConsumer {

    private final static String EXCHANGE_NAME = "trades";

    Channel channel = RabbitMQUtils.createChannel();

    public RabbitMQConsumer() throws IOException, TimeoutException {
    }


    void returnMessage() throws IOException, TimeoutException, InterruptedException {
        int partitionNumber = Infra.readFromApplicationPropertiesIntegerFormat("numberOfQueues");
        // Declare an exchange and queue, then bind them
        Channel channel = RabbitMQUtils.createChannel();

            // Declare an exchange and a single queue
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // Declare a single queue
            String queueName = "all_partitions_queue";
            channel.queueDeclare(queueName, true, false, false, null);

            // Bind the queue to all three partitions
        for (int i = 0; i < partitionNumber; i++) {
            channel.queueBind(queueName, EXCHANGE_NAME, "cc_partition_" + i);
        }

            System.out.println(" [*] Waiting for messages in '" + queueName + "'.");

            // Callback to handle the messages
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "' with routing key '" + delivery.getEnvelope().getRoutingKey() + "'");
                // Add logic here to process the transaction
            };

            // Start consuming messages
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });

            // Use a CountDownLatch to wait indefinitely
            CountDownLatch latch = new CountDownLatch(1);
            latch.await(); // This will block the main thread forever until countDown() is called
    }
}
