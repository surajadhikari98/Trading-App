package io.reactivestax;


import io.reactivestax.service.ChunkGeneratorService;
import io.reactivestax.service.ChunkProcessorService;
import io.reactivestax.factory.BeanFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.reactivestax.factory.BeanFactory.readFromApplicationPropertiesStringFormat;

@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {
        startProducer();
    }

    private static void startProducer() throws Exception {
        log.info("Starting in Producer Mode...");

        new ChunkGeneratorService().
                generateAndSubmitChunks(readFromApplicationPropertiesStringFormat("trade.file.path"),
                        BeanFactory.readFromApplicationPropertiesIntegerFormat("number.chunks"));

        //process chunks
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(Integer.parseInt(readFromApplicationPropertiesStringFormat("chunk.processor.thread.pool.size")));
        ChunkProcessorService chunkProcessorService = new ChunkProcessorService(chunkProcessorThreadPool);
        chunkProcessorService.processChunk();

    }

}



