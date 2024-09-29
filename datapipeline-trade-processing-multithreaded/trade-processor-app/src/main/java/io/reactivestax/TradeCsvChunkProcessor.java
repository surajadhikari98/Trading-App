package io.reactivestax;

import io.reactivestax.hikari.DataSource;

import java.io.*;
import java.sql.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TradeCsvChunkProcessor implements ChunkProcessor {

    static Connection connection;
    int numberOfChunks;
    ExecutorService chunkProcessorThreadPool;
    static ConcurrentHashMap<String, Integer> queueDistributorMap = new ConcurrentHashMap();
    static LinkedBlockingQueue<String> queue1 = new LinkedBlockingQueue<>();
    static LinkedBlockingQueue<String> queue2 = new LinkedBlockingQueue<>();
    static LinkedBlockingQueue<String> queue3 = new LinkedBlockingQueue<>();
    static AtomicInteger currentQueueIndex = new AtomicInteger(0);
    private CyclicBarrier barrier;

    static {
        try {
            connection = DataSource.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TradeCsvChunkProcessor(ExecutorService chunkProcessorThreadPool, int numberOfChunks, CyclicBarrier barrier) {
        this.chunkProcessorThreadPool = chunkProcessorThreadPool;
        this.numberOfChunks = numberOfChunks;
        this.barrier = barrier;
    }

//    public void processChunks() {
//        try {
//            for (int i = 0; i <= 10; i++) {
//                String chunkFileName = "trades_chunk_" + i + ".csv";
//                chunkProcessorThreadPool.submit(() -> {
//                    try {
//                        processChunkFiles(chunkFileName);
//                    } catch (IOException | SQLException | InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            System.out.println("done insertion in the trade payload");
////            chunkProcessorThreadPool.shutdown();
//        }
////        ArrayList<LinkedBlockingQueue<String>> queues = new ArrayList<>();
////        queues.add(queue1);
////        queues.add(queue2);
////        queues.add(queue3);
////        return queues;
//    }


    public void processChunks() {
        try {
            for (int i = 1; i <= numberOfChunks; i++) {
                String chunkFileName = "trades_chunk_" + i + ".csv";
                chunkProcessorThreadPool.submit(() -> {
                    try {
                        processChunkFiles(chunkFileName);
                    } catch (IOException | SQLException | InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        try {
                            barrier.await();
                        } catch (InterruptedException | BrokenBarrierException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void processChunkFiles(String fileName) throws IOException, SQLException, InterruptedException {
        try {
            insertIntoTradePayload(fileName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        System.out.println("Succefully inserted into db." + insertIntoTradePayload(fileName));
        System.out.println("queue1 size" + queue1.size());
        System.out.println("queue2 size" + queue2.size());
        System.out.println("queue3 size" + queue3.size());
        System.out.println("Map size" + queueDistributorMap.size());
        //maintain the Map for inserting into the queue
    }

    private int insertIntoTradePayload(String fileName) throws SQLException, IOException, InterruptedException {
//        String filePath = "/Users/Suraj.Adhikari/sources/student-mode-programs/boca-bc24-java-core-problems/src/problems/thread/distributedtrade/tradefiles/";
        String filePath = "C:\\Users\\suraj\\source\\full-stack-student-mode\\suad-bootcamp-2024\\datapipeline-trade-processing-multithreaded\\trade-processor-app\\src\\main\\java\\io\\reactivestax\\files\\";
        String insertQuery = "INSERT INTO trade_payloads (trade_id, status, status_reason, payload) VALUES (?, ?, ?,?)";
        PreparedStatement statement = connection.prepareStatement(insertQuery);
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath + fileName))) {
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                statement.setString(1, split[0]);
                statement.setString(2, checkValidity(split) ? "valid" : "inValid");
                statement.setString(3, checkValidity(split) ? "All field present " : "Fields missing");
                statement.setString(4, line);
                statement.addBatch();
                writeToTradeQueue(split);
            }
            int[] ints = statement.executeBatch();
            return ints.length;
        }
    }

//    @Override
//    public void writeToTradeQueue(String[] trade) throws InterruptedException {
//        if (queueDistributorMap.get(trade[2]) == null) {
//            int queueNumber = ThreadLocalRandom.current().nextInt(1, 4); //generating the random no from 1 to 3 in thread safe manner
//            queueDistributorMap.putIfAbsent(trade[2], queueNumber);
//            selectQueue(trade[0], queueNumber);
//        }
//        //consulting with the map for the insertion in the array blocking queue
//        if (queueDistributorMap.get(trade[2]) != null) {
//            Integer queueNumber = queueDistributorMap.get(trade[2]);
//            selectQueue(trade[0], queueNumber);
//        }
//
//
//    }

    @Override
    public void writeToTradeQueue(String[] trade) throws InterruptedException {
        int queueNumber = queueDistributorMap.computeIfAbsent(trade[2], k -> currentQueueIndex.incrementAndGet() % 3 + 1);
        selectQueue(trade[0], queueNumber);
        System.out.println("Assigned trade ID: " + trade[0] + " to queue: " + queueNumber);
    }

    private static void selectQueue(String tradeId, Integer queueNumber) throws InterruptedException {
        switch (queueNumber) {
            case 1:
                queue1.put(tradeId);
                break;
            case 2:
                queue2.put(tradeId);
                break;
            case 3:
                queue3.put(tradeId);
                break;
        }
    }

    private static boolean checkValidity(String[] split) {
        return (split[0]) != null;
    }

    public void startMultiThreadsForReadingFromQueue(ExecutorService executorService) throws Exception {
        //Start multiple consumer threads to process transactions
//        ExecutorService executorService = Executors.newFixedThreadPool(3);
            executorService.submit(new TradeProcessor(queue1));
            executorService.submit(new TradeProcessor(queue2));
            executorService.submit( new TradeProcessor(queue3));
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

}
