package io.reactivestax.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQProducer {

    private static final String EXCHANGE_NAME = "credit_card_transactions";

    public static void main(String[] argv) throws Exception {
        // Setup connection factory
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // Or the RabbitMQ server IP/hostname
        factory.setUsername("guest"); // RabbitMQ username
        factory.setPassword("guest"); // RabbitMQ password

        // Establish connection and create channel
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            // Declare an exchange of type direct (or other types based on your routing
            // strategy)
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // Publish multiple messages (e.g., credit card transactions)
            for (int i = 1; i <= 1000; i++) {
                String routingKey = getRoutingKeyBasedOnCreditCard(i);
                String message = "Transaction #" + i + " - Amount: " + (100 + i);
                channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "' with routing key '" + routingKey + "'");
            }
        }
    }

    // Simulate getting routing key based on credit card number
    private static String getRoutingKeyBasedOnCreditCard(int transactionId) {
        // For simplicity, route based on the transaction ID, e.g., to mimic credit card
        // partitioning
        return "cc_partition_" + (transactionId % 3);
    }
}
