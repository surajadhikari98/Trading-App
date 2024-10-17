package io.reactivestax;

import com.rabbitmq.client.Channel;
import io.reactivestax.component.TradeCsvChunkGenerator;
import io.reactivestax.component.TradeCsvChunkProcessor;
import io.reactivestax.infra.Infra;
import io.reactivestax.utils.RabbitMQUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        //start chunkGenerator
        new TradeCsvChunkGenerator().generateAndSubmitChunks(Infra.readFromApplicationPropertiesStringFormat("trade.file.path"), Infra.readFromApplicationPropertiesIntegerFormat("number.chunks"));

        //process chunks
        Infra.setUpQueue();
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(Integer.parseInt(Infra.readFromApplicationPropertiesStringFormat("chunk.processor.thread.pool.size")));
        TradeCsvChunkProcessor tradeCsvChunkProcessor = new TradeCsvChunkProcessor(chunkProcessorThreadPool);
        tradeCsvChunkProcessor.processChunk();


        //process trades
        ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(Infra.readFromApplicationPropertiesStringFormat("tradeProcessorThreadPoolSize")));
        tradeCsvChunkProcessor.startMultiThreadsForTradeProcessor(executorService);
    }
}



