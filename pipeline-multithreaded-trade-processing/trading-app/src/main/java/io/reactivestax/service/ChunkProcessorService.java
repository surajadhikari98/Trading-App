package io.reactivestax.service;

import io.reactivestax.contract.ChunkProcessor;
import io.reactivestax.contract.repository.PayloadRepository;
import io.reactivestax.contract.repository.TransactionUtil;
import io.reactivestax.domain.Trade;
import io.reactivestax.factory.BeanFactory;
import io.reactivestax.infra.Infra;
import lombok.extern.slf4j.Slf4j;


import java.io.*;
import java.util.List;
import java.util.concurrent.*;

import static io.reactivestax.utils.Utility.prepareTrade;

@Slf4j
public class ChunkProcessorService implements ChunkProcessor {

    ExecutorService chunkProcessorThreadPool;
    List<LinkedBlockingDeque<String>> queueTracker;

    public ChunkProcessorService(ExecutorService chunkProcessorThreadPool) {
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
        PayloadRepository tradePayloadRepository = BeanFactory.getTradePayloadRepository();
        TransactionUtil transactionUtil = BeanFactory.getTransactionUtil();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    Trade trade = prepareTrade(line);
                    transactionUtil.startTransaction();
                    tradePayloadRepository.insertTradeIntoTradePayloadTable(line);
                    transactionUtil.commitTransaction();
                    MessagePublisherService.figureTheNextQueue(trade);
                }
            }
        }


    public void startMultiThreadsForTradeProcessor(ExecutorService executorService) throws FileNotFoundException {
        for (int i = 0; i < Infra.readFromApplicationPropertiesIntegerFormat("number.queues"); i++) {
            executorService.submit(
                    new TradeProcessorService(Infra.readFromApplicationPropertiesStringFormat("queue.name") + i));
        }
        executorService.shutdown();
    }

}
