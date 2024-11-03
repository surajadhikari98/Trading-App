package io.reactivestax.rabbitmq;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RabbitMQConsumerApp {

    public static void main(String[] args) {
        // Create an ExecutorService with a single thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        // Submit the consumer task
        RabbitMQConsumerCallable consumerTask = new RabbitMQConsumerCallable("cc_partition_1_queue");
        Future<Void> consumerFuture = executorService.submit(consumerTask);

        // Register a shutdown hook to catch Ctrl-C (SIGINT) and shutdown the executor
        // gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown signal received. Stopping consumer...");
            executorService.shutdownNow(); // Issue shutdown to stop the thread
            try {
                if (!executorService.isTerminated()) {
                    executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                System.out.println("Shutdown interrupted.");
            }
            System.out.println("Consumer stopped.");
        }));

        // Keep the main thread alive until shutdown is triggered
        try {
            consumerFuture.get(); // Block until the consumer thread completes or is interrupted
        } catch (Exception e) {
            System.out.println("Exception while waiting for consumer to finish: " + e.getMessage());
        }
    }
}
