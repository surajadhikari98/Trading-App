package io.reactivestax.component;

import io.reactivestax.contract.ChunkProcessor;
import io.reactivestax.hikari.DataSource;
import io.reactivestax.infra.Infra;

import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TradeCsvChunkProcessor implements ChunkProcessor {

    static Connection connection;
    int numberOfChunks;
    ExecutorService chunkProcessorThreadPool;
    static ConcurrentHashMap<String, Integer> queueDistributorMap = new ConcurrentHashMap<>();
    //    Map<String, LinkedBlockingDeque<String>> queueTracker;
    List<LinkedBlockingDeque<String>> queueTracker;
    static LinkedBlockingDeque<String> queue1 = new LinkedBlockingDeque<>();
    static LinkedBlockingDeque<String> queue2 = new LinkedBlockingDeque<>();
    static LinkedBlockingDeque<String> queue3 = new LinkedBlockingDeque<>();
    static AtomicInteger currentQueueIndex = new AtomicInteger(0);

//    static {
//        try {
//            connection = DataSource.getConnection();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    public TradeCsvChunkProcessor(ExecutorService chunkProcessorThreadPool, int numberOfChunks, List<LinkedBlockingDeque<String>> queueTracker) {
        this.chunkProcessorThreadPool = chunkProcessorThreadPool;
        this.numberOfChunks = numberOfChunks;
        this.queueTracker = queueTracker;
    }

    public void processChunks() {
        try {
            for (int i = 1; i <= numberOfChunks; i++) {
//                String chunkFileName = "trades_chunk_" + i + ".csv";
                //consulting to the queue for reading the chunksFile
                String chunkFileName = Infra.getChunksFileMappingQueue().take();
                chunkProcessorThreadPool.submit(() -> {
                    try {
                        insertIntoTradePayload(chunkFileName);
                    } catch (IOException | SQLException | InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
//            System.out.println("queue 1 size" + queue1.size());
//            System.out.println("queue 2 size" + queue2.size());
//            System.out.println("queue 3 size" + queue3.size());

//            for (int i = 0; i < queueTracker.size(); i++) {
//                System.out.println(queueTracker.get("queues"+i) + "size" + queueTracker.get("queues"+i).size());
//            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void insertIntoTradePayload(String filePath) throws Exception {
//        String filePath = "src/main/java/io/reactivestax/files/";
        String insertQuery = "INSERT INTO trade_payloads (trade_id, status, status_reason, payload) VALUES (?, ?, ?,?)";
        PreparedStatement statement = DataSource.getConnection().prepareStatement(insertQuery);
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                statement.setString(1, split[0]);
                statement.setString(2, checkValidity(split) ? "valid" : "inValid");
                statement.setString(3, checkValidity(split) ? "All field present " : "Fields missing");
                statement.setString(4, line);
                statement.executeUpdate();
                writeToTradeQueue(split);
            }
        }
    }

    @Override
    // Get the queue number, or assign one in a round-robin manner if not already assigned
    public void writeToTradeQueue(String[] trade) throws InterruptedException, FileNotFoundException {
        String distributionCriteria = Infra.readFromApplicationProperties("tradeDistributionCriteria");
        //checking the distributionCriteria from Application.properties
        int queueNumber = queueDistributorMap.computeIfAbsent(distributionCriteria.equals("accountNumber") ? trade[2] : trade[0],
                k -> (currentQueueIndex.incrementAndGet() % 3) + 1); //generate 1,2,3
        selectQueue(trade[0], queueNumber);
        System.out.println("Assigned trade ID: " + trade[0] + " to queue: " + queueNumber);
    }

//    private void selectQueue(String tradeId, Integer queueNumber) throws InterruptedException {
//
//        switch (queueNumber) {
//            case 1:
//                queueTracker.get(0).put(tradeId);
//                System.out.println("queueTracker1.size() = " + queueTracker.get(0).size());
//                break;
//            case 2:
//                queueTracker.get(1).put(tradeId);
//                System.out.println("queueTracker2.size() = " + queueTracker.get(1).size());
//                break;
//            case 3:
//                queueTracker.get(2).put(tradeId);
//                System.out.println("queue3.size() = " + queueTracker.get(2).size());
//                break;
//            default:
//                throw new IllegalStateException("Unexpected value: " + queueNumber);
//        }
//    }

    private void selectQueue(String tradeId, Integer queueNumber) throws InterruptedException {
        queueTracker.get(queueNumber - 1).put(tradeId);
        System.out.println(queueNumber + "size is: " + queueTracker.get(queueNumber - 1).size());
    }

    private static boolean checkValidity(String[] split) {
        return (split[0]) != null;
    }

    public void startMultiThreadsForTradeProcessor(ExecutorService executorService) throws Exception {
        for (int i = 0; i < queueTracker.size(); i++) {
            CsvTradeProcessor csvTradeProcessor = new CsvTradeProcessor(queueTracker.get(i));
            executorService.submit(csvTradeProcessor);
        }
        executorService.shutdown();
    }
}
