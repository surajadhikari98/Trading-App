package io.reactivestax;


import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
//        new TradeCsvChunkGenerator().generateChunk("/Users/Suraj.Adhikari/downloads/trades.csv");
        new TradeCsvChunkGenerator().generateChunk("C:\\Users\\suraj\\Downloads\\csv\\trades.csv");
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(10);
        CyclicBarrier barrier = new CyclicBarrier(10, () -> System.out.println("All chunks processed. Starting consumer threads."));
        TradeCsvChunkProcessor tradeCsvChunkProcessor = new TradeCsvChunkProcessor(chunkProcessorThreadPool, 10, barrier);
        tradeCsvChunkProcessor.processChunks();
//        tradeCsvChunkProcessor.startMultiThreadsForReadingFromQueue();

        // Wait for chunk processing to finish
        barrier.await();

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        tradeCsvChunkProcessor.startMultiThreadsForReadingFromQueue(executorService);

//Thread.sleep(10000);
//
//        executorService.submit(new TradeProcessor(queueList[0]));
//        executorService.submit(new TradeProcessor(queueList[1]));
//        executorService.submit( new TradeProcessor(queueList[2]));



        //calling for tradeProcessor for passing and reading the queue
//        chunkProcessorImpl.startMultiThreadsForReadingFromQueue();
//        LinkedBlockingQueue<String> strings = new LinkedBlockingQueue<>();
//        strings.add("TDB_00000999");
//        strings.add("TDB_00009997");
//        strings.add("TDB_00011000");
//        new TradeProcessor(strings).readFromQueueAndQueryPayload();
    }
}
