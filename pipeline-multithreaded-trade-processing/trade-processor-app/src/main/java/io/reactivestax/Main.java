package io.reactivestax;

import com.rabbitmq.client.Channel;
import io.reactivestax.component.TradeCsvChunkGenerator;
import io.reactivestax.component.TradeCsvChunkProcessor;
import io.reactivestax.infra.Infra;
import io.reactivestax.utils.RabbitMQUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class Main {

    public static void main(String[] args) throws Exception {
        //start chunkGenerator
        new TradeCsvChunkGenerator().generateAndSubmitChunks(Infra.readFromApplicationPropertiesStringFormat("tradeFilePath"), Infra.readFromApplicationPropertiesIntegerFormat("numberOfChunks"));

        //process chunks
//        List<LinkedBlockingDeque<String>> queues = Infra.addToQueueList();
        Channel channel = RabbitMQUtils.getInstance().getChannel();
        Infra.setUpQueue(channel);
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(Integer.parseInt(Infra.readFromApplicationPropertiesStringFormat("chunkProcessorThreadPoolSize")));
        TradeCsvChunkProcessor tradeCsvChunkProcessor = new TradeCsvChunkProcessor(chunkProcessorThreadPool);
        tradeCsvChunkProcessor.processChunk();


        //process trades
        ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(Infra.readFromApplicationPropertiesStringFormat("tradeProcessorThreadPoolSize")));
        tradeCsvChunkProcessor.startMultiThreadsForTradeProcessor(executorService);
    }
}



