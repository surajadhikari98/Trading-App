package io.reactivestax.component;

import io.reactivestax.contract.ChunkProcessor;
import io.reactivestax.hikari.DBUtils;
import io.reactivestax.hikari.DataSource;
import io.reactivestax.infra.Infra;
import io.reactivestax.repository.TradePayloadRepository;
import io.reactivestax.utils.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.reactivestax.utils.Utility.checkValidity;

public class TradeCsvChunkProcessor implements ChunkProcessor {

    int numberOfChunks;
    ExecutorService chunkProcessorThreadPool;
    static ConcurrentHashMap<String, Integer> queueDistributorMap = new ConcurrentHashMap<>();
    List<LinkedBlockingDeque<String>> queueTracker;
    private static final Logger logger = LoggerFactory.getLogger(TradeCsvChunkProcessor.class);


    public TradeCsvChunkProcessor(ExecutorService chunkProcessorThreadPool, int numberOfChunks, List<LinkedBlockingDeque<String>> queueTracker) {
        this.chunkProcessorThreadPool = chunkProcessorThreadPool;
        this.numberOfChunks = numberOfChunks;
        this.queueTracker = queueTracker;
    }

    @Override
    public void processChunk() throws Exception {
        int chunkProcessorThreadPoolSize = Infra.readFromApplicationPropertiesIntegerFormat("chunkProcessorThreadPoolSize");
        for (int i = 0; i < chunkProcessorThreadPoolSize; i++) {
            //consulting to the queue for reading the chunksFile
            String chunkFileName = Infra.getChunksFileMappingQueue().take();
            chunkProcessorThreadPool.submit(() -> {
                try {
                    insertTradeIntoTradePayloadTable(chunkFileName);
                } catch (Exception e) {
                    logger.error("error while insert into trade payloads = {}", e.getMessage());
                }
            });
        }
    }

    public void insertTradeIntoTradePayloadTable(String filePath) throws Exception {
        String line;
        TradePayloadRepository tradePayloadRepository = new TradePayloadRepository(DataSource.getConnection());
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                tradePayloadRepository.insertTradeIntoTradePayloadTable(line);
                figureTheNextQueue(split);
            }
        }
    }

    // Get the queue number, or assign one in a round-robin or random manner based on application-properties
    public void figureTheNextQueue(String[] trade) throws InterruptedException, FileNotFoundException {
        String distributionCriteria = Infra.readFromApplicationPropertiesStringFormat("distributionLogic.Criteria");
        String useMap = Infra.readFromApplicationPropertiesStringFormat("distributionLogic.useMap");
        String distributionAlgorithm = Infra.readFromApplicationPropertiesStringFormat("distributionLogic.algorithm");


        if (Boolean.parseBoolean(useMap)) {
            int queueNumber = 0;
            if (Objects.equals(distributionAlgorithm, "round-robin")) {
                queueNumber = queueDistributorMap.computeIfAbsent(distributionCriteria.equals("accountNumber") ? trade[2] : trade[0],
                        k -> Utility.roundRobin()); //generate 1,2,3
            } else if (Objects.equals(distributionAlgorithm, "random")) {
                queueNumber = queueDistributorMap.computeIfAbsent(distributionCriteria.equals("accountNumber") ? trade[2] : trade[0],
                        k -> Utility.random()); //generate 1,2,3
            }
            selectAndPutInQueue(trade[0], queueNumber);
            logger.info("Assigned trade ID: {} to queue: {}", trade[0], queueNumber);
        }

        if (!Boolean.parseBoolean(useMap)) {
            int queueNumber = 0;
            if (distributionAlgorithm.equals("round-robin")) {
                queueNumber = Utility.roundRobin();
            }

            if (distributionAlgorithm.equals("random")) {
                queueNumber = Utility.random();
            }
            selectAndPutInQueue(trade[0], queueNumber);
            logger.info("Assigned trade ID: {} to queue : {} " , trade[0] , queueNumber);
        }

    }

    private void selectAndPutInQueue(String tradeId, Integer queueNumber) throws InterruptedException {
        queueTracker.get(queueNumber - 1).put(tradeId);
        logger.info("{} size is: {}", queueNumber,  queueTracker.get(queueNumber - 1).size());
    }

    public void startMultiThreadsForTradeProcessor(ExecutorService executorService) throws Exception {
        for (LinkedBlockingDeque<String> queues : queueTracker) {
            CsvTradeProcessor csvTradeProcessor = new CsvTradeProcessor(queues);
            executorService.submit(csvTradeProcessor);
        }
        executorService.shutdown();
    }

}
