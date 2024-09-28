package io.reactivestax;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

//    private static final CyclicBarrier barrier;

//    static{
//        barrier = new CyclicBarrier(11, () -> System.out.println("All chunks_processor have reached the barrier. Proceeding to start the consumer part"));
//    }

    public static void main(String[] args) throws Exception {
        new ChunkGeneratorImpl().generateChunk("/Users/Suraj.Adhikari/downloads/trades.csv");
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(10);
        ChunkProcessorImpl chunkProcessorImpl = new ChunkProcessorImpl(chunkProcessorThreadPool, 10);
        chunkProcessorImpl.processChunks();
        chunkProcessorImpl.startMultiThreadsForReadingFromQueue();


        //calling for tradeProcessor for passing and reading the queue
//        chunkProcessorImpl.startMultiThreadsForReadingFromQueue();
//        LinkedBlockingQueue<String> strings = new LinkedBlockingQueue<>();
//        strings.add("TDB_00000999");
//        strings.add("TDB_00009997");
//        strings.add("TDB_00011000");
//        new TradeProcessor(strings).readFromQueueAndQueryPayload();
    }
}
