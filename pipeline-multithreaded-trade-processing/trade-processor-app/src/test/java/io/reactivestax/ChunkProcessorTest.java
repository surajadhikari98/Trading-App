package io.reactivestax;

import io.reactivestax.service.TradeCsvChunkProcessor;
import io.reactivestax.hikari.DataSource;
import io.reactivestax.infra.Infra;
import io.reactivestax.repository.TradePayloadRepository;
import org.junit.Test;

import java.sql.Connection;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class ChunkProcessorTest {


    @Test
    public void testInsertionInTradePayloadTable() throws Exception {
        Connection connection = DataSource.getConnection();
        TradePayloadRepository tradePayloadRepository = new TradePayloadRepository(connection);
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(Integer.parseInt(Infra.readFromApplicationPropertiesStringFormat("chunk.processor.thread.pool.size")));

        TradeCsvChunkProcessor tradeCsvChunkProcessor = new TradeCsvChunkProcessor(chunkProcessorThreadPool);
        tradeCsvChunkProcessor.insertTradeIntoTradePayloadTable("src/test/resources/test_trade.csv");
        int recordsInserted = tradePayloadRepository.selectTradePayload();
        assertEquals(7, recordsInserted);
        assert connection != null;
        new HelperClass(connection).clearTable("trade_payloads");
    }
}
