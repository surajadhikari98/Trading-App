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
        //start chunkGenerator
        new TradeCsvChunkGenerator().generateChunk(Infra.readFromApplicationProperties("tradeFilePath"));
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(10);

        //Process chunks
        Map<String, LinkedBlockingDeque<String>> queueMap = Infra.addToQueueMap();
        TradeCsvChunkProcessor tradeCsvChunkProcessor = new TradeCsvChunkProcessor(chunkProcessorThreadPool, 10, queueMap);
        tradeCsvChunkProcessor.processChunks();


        //process trades
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        tradeCsvChunkProcessor.startMultiThreadsForReadingFromQueue(executorService);

    }
}



