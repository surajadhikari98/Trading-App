package io.reactivestax.component;

import io.reactivestax.contract.ChunkProcessor;
import io.reactivestax.hikari.DataSource;
import io.reactivestax.infra.Infra;

import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TradeCsvChunkProcessor implements ChunkProcessor {

    int numberOfChunks;
    ExecutorService chunkProcessorThreadPool;
    static ConcurrentHashMap<String, Integer> queueDistributorMap = new ConcurrentHashMap<>();
    List<LinkedBlockingDeque<String>> queueTracker;
    static AtomicInteger currentQueueIndex = new AtomicInteger(0);

    public TradeCsvChunkProcessor(ExecutorService chunkProcessorThreadPool, int numberOfChunks, List<LinkedBlockingDeque<String>> queueTracker) {
        this.chunkProcessorThreadPool = chunkProcessorThreadPool;
        this.numberOfChunks = numberOfChunks;
        this.queueTracker = queueTracker;
    }

    @Override
    public void processChunk() {
        try {
            for (int i = 1; i <= numberOfChunks; i++) {
//                String chunkFileName = "trades_chunk_" + i + ".csv";
                //consulting to the queue for reading the chunksFile
                String chunkFileName = Infra.getChunksFileMappingQueue().take();
                chunkProcessorThreadPool.submit(() -> {
                    try {
                        insertTradeIntoTradePayloadTable(chunkFileName);
                    } catch (IOException | SQLException | InterruptedException e) {
                        System.out.println("error while insert into trade payloads = " + e.getMessage());
//                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        System.out.println("error while insert into trade payloads = " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
@Override
public void insertTradeIntoTradePayloadTable(String filePath) throws Exception {
    String insertQuery = "INSERT INTO trade_payloads (trade_id, validity_status, status_reason, lookup_status, je_status, payload) VALUES (?, ?, ?, ?, ?, ?)";

        try(PreparedStatement statement = DataSource.getConnection().prepareStatement(insertQuery)) {
            String line;
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(",");
                    statement.setString(1, split[0]);
                    statement.setString(2, checkValidity(split) ? "valid" : "inValid");
                    statement.setString(3, checkValidity(split) ? "All field present " : "Fields missing");
                    statement.setString(4, "");
                    statement.setString(5, "not_posted");
                    statement.setString(6, line);
                    statement.executeUpdate();
                    writeToTradeQueue(split);
                }
            }
        }
    }



    // Get the queue number, or assign one in a round-robin manner if not already assigned
    public void writeToTradeQueue(String[] trade) throws InterruptedException, FileNotFoundException {
        String distributionCriteria = Infra.readFromApplicationPropertiesStringFormat("tradeDistributionCriteria");
        //checking the distributionCriteria from Application.properties
        int queueNumber = queueDistributorMap.computeIfAbsent(distributionCriteria.equals("accountNumber") ? trade[2] : trade[0],
                k -> (currentQueueIndex.incrementAndGet() % 3) + 1); //generate 1,2,3
        selectAndPutInQueue(trade[0], queueNumber);
        System.out.println("Assigned trade ID: " + trade[0] + " to queue: " + queueNumber);
    }

    private void selectAndPutInQueue(String tradeId, Integer queueNumber) throws InterruptedException {
        queueTracker.get(queueNumber - 1).put(tradeId);
        System.out.println(queueNumber + " size is: " + queueTracker.get(queueNumber - 1).size());
    }

    private static boolean checkValidity(String[] split) {
        return (split[0]) != null;
    }

    public void startMultiThreadsForTradeProcessor(ExecutorService executorService) throws Exception {
        for (LinkedBlockingDeque<String> strings : queueTracker) {
            CsvTradeProcessor csvTradeProcessor = new CsvTradeProcessor(strings);
            executorService.submit(csvTradeProcessor);
        }
        executorService.shutdown();
    }

}
