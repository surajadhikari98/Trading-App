package io.reactivestax;

import io.reactivestax.service.TradeCsvChunkGenerator;
import io.reactivestax.service.TradeCsvChunkProcessor;
import io.reactivestax.infra.Infra;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        //start chunkGenerator
        new TradeCsvChunkGenerator().generateAndSubmitChunks(Infra.readFromApplicationPropertiesStringFormat("trade.file.path"), Infra.readFromApplicationPropertiesIntegerFormat("number.chunks"));

        //process chunks
        Infra.setUpQueue();
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(Integer.parseInt(Infra.readFromApplicationPropertiesStringFormat("chunk.processor.thread.pool.size")));
        TradeCsvChunkProcessor tradeCsvChunkProcessor = new TradeCsvChunkProcessor(chunkProcessorThreadPool);
        tradeCsvChunkProcessor.processChunk();


        //process trades
        ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(Infra.readFromApplicationPropertiesStringFormat("tradeProcessorThreadPoolSize")));
        tradeCsvChunkProcessor.startMultiThreadsForTradeProcessor(executorService);
    }
}



