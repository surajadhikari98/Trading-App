package io.reactivestax;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        new TradeCsvChunkGenerator().generateChunk("/Users/Suraj.Adhikari/downloads/trades.csv");
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(10);
//        CyclicBarrier barrier = new CyclicBarrier(10);
        TradeCsvChunkProcessor tradeCsvChunkProcessor = new TradeCsvChunkProcessor(chunkProcessorThreadPool, 10);
        tradeCsvChunkProcessor.processChunks();

//        // Wait for chunk processing to finish
//        barrier.await();
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        tradeCsvChunkProcessor.startMultiThreadsForReadingFromQueue(executorService);

    }
}



