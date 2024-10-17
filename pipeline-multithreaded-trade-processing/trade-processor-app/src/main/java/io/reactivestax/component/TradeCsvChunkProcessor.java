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
import java.util.concurrent.*;

@Slf4j
public class TradeCsvChunkProcessor implements ChunkProcessor {

    ExecutorService chunkProcessorThreadPool;

    public TradeCsvChunkProcessor(ExecutorService chunkProcessorThreadPool) {
        this.chunkProcessorThreadPool = chunkProcessorThreadPool;
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
        try (Channel channel = RabbitMQUtils.getInstance().createChannel()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(",");
                    TradePayloadCRUD.persistTradePayload(session, line);
                    RabbitMQProducer.figureTheNextQueue(split, channel);
                }
            }
        }
    }


    public void startMultiThreadsForTradeProcessor(ExecutorService executorService) throws FileNotFoundException {
        for (int i = 0; i < Infra.readFromApplicationPropertiesIntegerFormat("numberOfQueues"); i++) {
            executorService.submit(new CsvTradeProcessor(Infra.readFromApplicationPropertiesStringFormat("rabbitMQ.queue.name") + i));
        }
        executorService.shutdown();
    }

}
