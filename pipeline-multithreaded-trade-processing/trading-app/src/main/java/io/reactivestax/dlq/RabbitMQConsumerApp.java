package io.reactivestax.dlq;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RabbitMQConsumerApp {

    public static void startConsumer(ExecutorService executorService, String queueName) {
        // Create an ExecutorService with a single thread

        // Submit the consumer task
        RabbitMQConsumerCallable consumerTask = new RabbitMQConsumerCallable(queueName);
        Future<Void> consumerFuture = executorService.submit(consumerTask);

        registerShutdownHooks(executorService);

        // Keep the main thread alive until shutdown is triggered
        try {
            consumerFuture.get(); // Block until the consumer thread completes or is interrupted
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception while waiting for consumer to finish: " + e.getMessage());
        }
    }

    private static void registerShutdownHooks(ExecutorService executorService) {
        // Register a shutdown hook to catch Ctrl-C (SIGINT) and shutdown the executor
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
    }
}
