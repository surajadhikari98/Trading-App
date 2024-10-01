package io.reactivestax;

import io.reactivestax.component.TradeCsvChunkGenerator;
import io.reactivestax.component.TradeCsvChunkProcessor;
import io.reactivestax.infra.Infra;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class Main {

    public static void main(String[] args) throws Exception {
        new TradeCsvChunkGenerator().generateChunk("/Users/Suraj.Adhikari/downloads/trades.csv");
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(10);
        Map<String, LinkedBlockingDeque<String>> queueMap = Infra.addToQueueMap();
        TradeCsvChunkProcessor tradeCsvChunkProcessor = new TradeCsvChunkProcessor(chunkProcessorThreadPool, 10, queueMap);
        tradeCsvChunkProcessor.processChunks();


        ExecutorService executorService = Executors.newFixedThreadPool(3);
        tradeCsvChunkProcessor.startMultiThreadsForReadingFromQueue(executorService);

    }
}



