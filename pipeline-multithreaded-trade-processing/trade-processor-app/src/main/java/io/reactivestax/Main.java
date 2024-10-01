package io.reactivestax;

import io.reactivestax.component.TradeCsvChunkGenerator;
import io.reactivestax.component.TradeCsvChunkProcessor;
import io.reactivestax.infra.Infra;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        //start chunkGenerator
        new TradeCsvChunkGenerator().generateChunk(Infra.readFromApplicationProperties("tradeFilePath"));


        //process chunks
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(Integer.parseInt(Infra.readFromApplicationProperties("chunkProcessorThreadPoolSize")));
        TradeCsvChunkProcessor tradeCsvChunkProcessor = new TradeCsvChunkProcessor(chunkProcessorThreadPool, 10, Infra.addToQueueMap());
        tradeCsvChunkProcessor.processChunks();


        //process trades
        ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(Infra.readFromApplicationProperties("tradeProcessorThreadPoolSize")));
        tradeCsvChunkProcessor.startMultiThreadsForTradeProcessor(executorService);

    }
}



