package io.reactivestax;

import io.reactivestax.component.TradeCsvChunkGenerator;
import io.reactivestax.component.TradeCsvChunkProcessor;
import io.reactivestax.infra.Infra;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws Exception {
        //start chunkGenerator
        new TradeCsvChunkGenerator().generateAndSubmitChunks(Infra.readFromApplicationPropertiesStringFormat("tradeFilePath"), Infra.readFromApplicationPropertiesIntegerFormat("numberOfChunks"));

        //process chunks
        List<LinkedBlockingDeque<String>> queues = Infra.addToQueueList();
        int numberOfChunks = Infra.readFromApplicationPropertiesIntegerFormat("numberOfChunks");

        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(Integer.parseInt(Infra.readFromApplicationPropertiesStringFormat("chunkProcessorThreadPoolSize")));
        TradeCsvChunkProcessor tradeCsvChunkProcessor = new TradeCsvChunkProcessor(chunkProcessorThreadPool, numberOfChunks, queues);
        tradeCsvChunkProcessor.processChunk();


        //process trades
        ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(Infra.readFromApplicationPropertiesStringFormat("tradeProcessorThreadPoolSize")));
        tradeCsvChunkProcessor.startMultiThreadsForTradeProcessor(executorService);

    }
}



