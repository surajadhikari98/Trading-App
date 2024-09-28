package io.reactivestax;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

//    private static final CyclicBarrier barrier;

//    static{
//        barrier = new CyclicBarrier(11, () -> System.out.println("All chunks_processor have reached the barrier. Proceeding to start the consumer part"));
//    }

    public static void main(String[] args) throws Exception {
        new TradeCsvChunkGenerator().generateChunk("/Users/Suraj.Adhikari/downloads/trades.csv");
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(10);
        TradeCsvChunkProcessor tradeCsvChunkProcessor = new TradeCsvChunkProcessor(chunkProcessorThreadPool, 10);
        tradeCsvChunkProcessor.processChunks();
        tradeCsvChunkProcessor.startMultiThreadsForReadingFromQueue();


        //calling for tradeProcessor for passing and reading the queue
//        chunkProcessorImpl.startMultiThreadsForReadingFromQueue();
//        LinkedBlockingQueue<String> strings = new LinkedBlockingQueue<>();
//        strings.add("TDB_00000999");
//        strings.add("TDB_00009997");
//        strings.add("TDB_00011000");
//        new TradeProcessor(strings).readFromQueueAndQueryPayload();
    }
}
