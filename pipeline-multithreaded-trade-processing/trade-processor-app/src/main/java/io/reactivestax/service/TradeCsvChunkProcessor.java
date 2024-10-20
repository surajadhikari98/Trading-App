package io.reactivestax.service;

import io.reactivestax.contract.ChunkProcessor;
import io.reactivestax.infra.Infra;
import lombok.extern.slf4j.Slf4j;


import java.io.*;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class TradeCsvChunkProcessor implements ChunkProcessor {

    ExecutorService chunkProcessorThreadPool;
    List<LinkedBlockingDeque<String>> queueTracker;

    public TradeCsvChunkProcessor(ExecutorService chunkProcessorThreadPool) {
        this.chunkProcessorThreadPool = chunkProcessorThreadPool;
    }


    @Override
    public void processChunk() throws Exception {
        int chunkProcessorThreadPoolSize = Infra.readFromApplicationPropertiesIntegerFormat("chunk.processor.thread.pool.size");
        for (int i = 0; i < chunkProcessorThreadPoolSize; i++) {
            //consulting to the queue for reading the chunksFile
            String chunkFileName = Infra.getChunksFileMappingQueue().take();
            chunkProcessorThreadPool.submit(() -> {
                try {
                    insertTradeIntoTradePayloadTable(chunkFileName);
                } catch (Exception e) {
                    log.info("error while insert into trade payloads {}", e.getMessage());
                }
            });
        }
    }


    public void insertTradeIntoTradePayloadTable(String filePath) throws Exception {
        String line;
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(",");
                    QueueDistributor.figureTheNextQueue(split);
                }
            }
        }


    public void startMultiThreadsForTradeProcessor(ExecutorService executorService) throws FileNotFoundException {
        for (int i = 0; i < Infra.readFromApplicationPropertiesIntegerFormat("number.queues"); i++) {
            executorService.submit(
                    new CsvTradeProcessor(Infra.readFromApplicationPropertiesStringFormat("queue.name") + i));
        }
        executorService.shutdown();
    }

}
