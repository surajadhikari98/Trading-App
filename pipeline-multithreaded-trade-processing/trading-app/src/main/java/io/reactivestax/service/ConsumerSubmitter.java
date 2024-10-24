package io.reactivestax.service;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
public class ConsumerSubmitter {

    private ConsumerSubmitter() {}

    public static void startConsumer(ExecutorService executorService, String queueName) {

        TradeProcessorService consumerTask = new TradeProcessorService(queueName);
        Future<Void> consumerFuture = executorService.submit(consumerTask);

        registerShutdownHooks(executorService);

        try {
            consumerFuture.get(); // Block until the consumer thread completes or is interrupted
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private static void registerShutdownHooks(ExecutorService executorService) {
        // Register a shutdown hook to catch Ctrl-C (SIGINT) and shutdown the executor
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown signal received. Stopping consumer...");
            executorService.shutdownNow(); // Issue shutdown to stop the thread
            try {
                if (!executorService.isTerminated()) {
                    executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage());
              log.info("Shutdown interrupted.");
            }
            log.info("Consumer stopped.");
        }));
    }
}
