package io.reactivestax;

import io.reactivestax.component.TradeCsvChunkProcessor;
import io.reactivestax.hikari.DataSource;
import io.reactivestax.infra.Infra;
import io.reactivestax.repository.TradePayloadRepository;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class ChunkProcessorTest {


    @Test
    public void testInsertionInTradePayloadTable() throws Exception {
        Connection connection = DataSource.getConnection();
        TradePayloadRepository tradePayloadRepository = new TradePayloadRepository(connection);
        List<LinkedBlockingDeque<String>> queues = Infra.addToQueueList();
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(Integer.parseInt(Infra.readFromApplicationPropertiesStringFormat("chunkProcessorThreadPoolSize")));

        TradeCsvChunkProcessor tradeCsvChunkProcessor = new TradeCsvChunkProcessor(chunkProcessorThreadPool, queues);
        tradeCsvChunkProcessor.insertTradeIntoTradePayloadTable("src/test/resources/test_trade.csv");
        int recordsInserted = tradePayloadRepository.selectTradePayload();
        assertEquals(7, recordsInserted);
        assert connection != null;
        new HelperClass(connection).clearTable("trade_payloads");
    }
}
