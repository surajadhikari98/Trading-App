package io.reactivestax;

import io.reactivestax.service.ConsumerSubmitter;
import io.reactivestax.service.dlq.RabbitMQConsumerApp;
import io.reactivestax.enums.AppModeEnum;
import io.reactivestax.service.ChunkGeneratorService;
import io.reactivestax.service.ChunkProcessorService;
import io.reactivestax.infra.Infra;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.reactivestax.infra.Infra.readFromApplicationPropertiesStringFormat;

@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {
        // Load properties (you can load this from the file if needed)

        String appMode = readFromApplicationPropertiesStringFormat("trading.app.mode"); //loading from application.properties

        if (appMode.equalsIgnoreCase(String.valueOf(AppModeEnum.PRODUCER))) {
            startProducer();
//            startConsumer();
        } else if (appMode.equalsIgnoreCase(String.valueOf(AppModeEnum.CONSUMER))) {
            startConsumer();
        } else {
            throw new IllegalArgumentException("Unknown mode: " + appMode);
        }
    }

    private static void startProducer() throws Exception {
        log.info("Starting in Producer Mode...");

        new ChunkGeneratorService().
                generateAndSubmitChunks(readFromApplicationPropertiesStringFormat("trade.file.path"),
                        Infra.readFromApplicationPropertiesIntegerFormat("number.chunks"));

        //process chunks
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(Integer.parseInt(readFromApplicationPropertiesStringFormat("chunk.processor.thread.pool.size")));
        ChunkProcessorService chunkProcessorService = new ChunkProcessorService(chunkProcessorThreadPool);
        chunkProcessorService.processChunk();

    }

    private static void startConsumer() throws FileNotFoundException {
        log.info("Starting in Consumer Mode...");
        //process trades
        ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(readFromApplicationPropertiesStringFormat("tradeProcessorThreadPoolSize")));
        for (int i = 0; i < Infra.readFromApplicationPropertiesIntegerFormat("number.queues"); i++) {
            ConsumerSubmitter.startConsumer(executorService, readFromApplicationPropertiesStringFormat("queue.name") + i);
        }
    }

}



