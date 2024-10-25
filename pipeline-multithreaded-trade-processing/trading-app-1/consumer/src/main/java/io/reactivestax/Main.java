package io.reactivestax;

import io.reactivestax.service.ConsumerSubmitterService;
import io.reactivestax.factory.BeanFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.reactivestax.factory.BeanFactory.readFromApplicationPropertiesStringFormat;

@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {
        startConsumer();
    }

    private static void startConsumer() throws FileNotFoundException {
        log.info("Starting in Consumer Mode...");
        ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(readFromApplicationPropertiesStringFormat("tradeProcessorThreadPoolSize")));
        for (int i = 0; i < BeanFactory.readFromApplicationPropertiesIntegerFormat("number.queues"); i++) {
            ConsumerSubmitterService.startConsumer(executorService, readFromApplicationPropertiesStringFormat("queue.name") + i);
        }
    }
}



