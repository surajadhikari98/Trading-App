package io.reactivestax.component;

import com.rabbitmq.client.Channel;
import io.reactivestax.contract.ChunkProcessor;
import io.reactivestax.infra.Infra;
import io.reactivestax.rabbitmq.RabbitMQProducer;
import io.reactivestax.repository.hibernate.crud.TradePayloadCRUD;
import io.reactivestax.utils.HibernateUtil;
import io.reactivestax.utils.RabbitMQUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;


import java.io.*;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class TradeCsvChunkProcessor implements ChunkProcessor {

    ExecutorService chunkProcessorThreadPool;
    static ConcurrentHashMap<String, Integer> queueDistributorMap = new ConcurrentHashMap<>();
    List<LinkedBlockingDeque<String>> queueTracker;

    public TradeCsvChunkProcessor(ExecutorService chunkProcessorThreadPool, List<LinkedBlockingDeque<String>> queueTracker) {
        this.chunkProcessorThreadPool = chunkProcessorThreadPool;
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
                    log.info("error while insert into trade payloads {}", e.getMessage());
                }
            });
        }
    }

    public void insertTradeIntoTradePayloadTable(String filePath) throws Exception {
        String line;
        Session session = HibernateUtil.getInstance().getSession();
        Channel channel = RabbitMQUtils.createChannel();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                //Hibernate way of insertion
                TradePayloadCRUD.persistTradePayload(session, line);
//                tradePayloadRepository.insertTradeIntoTradePayloadTable(line);
                RabbitMQProducer.figureTheNextQueue(split, channel);
            }
        }
    }



    public void startMultiThreadsForTradeProcessor(ExecutorService executorService) {
        for (LinkedBlockingDeque<String> queues : queueTracker) {
            CsvTradeProcessor csvTradeProcessor = new CsvTradeProcessor(queues);
            executorService.submit(csvTradeProcessor);
        }
        executorService.shutdown();
    }

}
