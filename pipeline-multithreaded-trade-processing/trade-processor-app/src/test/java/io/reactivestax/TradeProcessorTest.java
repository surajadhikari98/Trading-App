package io.reactivestax;

import io.reactivestax.component.CsvTradeProcessor;
import io.reactivestax.component.TradeCsvChunkProcessor;
import io.reactivestax.hikari.DataSource;
import io.reactivestax.infra.Infra;
import io.reactivestax.repository.CsvTradeProcessorRepository;
import io.reactivestax.repository.TradePositionRepository;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.Assert.assertEquals;

public class TradeProcessorTest {

    private final LinkedBlockingDeque<String> dequeue = new LinkedBlockingDeque<>();
    Connection connection = DataSource.getConnection();


    @Before
    public void setUp() throws Exception {
        dequeue.put("TDB_00000000");
        dequeue.put("TDB_00000001");
        dequeue.put("TDB_00000002");
        dequeue.put("TDB_00000003");
        dequeue.put("TDB_00000004");
        dequeue.put("TDB_00000005");
        dequeue.put("TDB_00000006");
        prepareTradePayloads();

    }

    @Test
    public void shouldCallStoredProcedureAndInsertIntoJournalAndPosition() throws Exception {
        CsvTradeProcessor csvTradeProcessor = new CsvTradeProcessor(dequeue);
        csvTradeProcessor.processTrade();
        int journalEntriesCount = new CsvTradeProcessorRepository(connection).getJournalEntriesCount();
        int positionCount = new TradePositionRepository(connection).getPositionCount();
        assertEquals(5, journalEntriesCount, positionCount);
        cleanUp(connection);
    }

       private void prepareTradePayloads() throws Exception {
        List<LinkedBlockingDeque<String>> queues = Infra.addToQueueList();
        ExecutorService chunkProcessorThreadPool = Executors.newFixedThreadPool(Integer.parseInt(Infra.readFromApplicationPropertiesStringFormat("chunkProcessorThreadPoolSize")));

        TradeCsvChunkProcessor tradeCsvChunkProcessor = new TradeCsvChunkProcessor(chunkProcessorThreadPool, 1, queues);
        tradeCsvChunkProcessor.insertTradeIntoTradePayloadTable("src/test/resources/test_trade.csv");
    }

    @Test
    public void testLookUpCheckAndDlQueueInsertion() throws Exception {
        CsvTradeProcessor csvTradeProcessor = new CsvTradeProcessor(dequeue);
        csvTradeProcessor.processTrade();
        int journalEntriesCount = new CsvTradeProcessorRepository(connection).getJournalEntriesCount();
        int positionCount = new TradePositionRepository(connection).getPositionCount();
        assertEquals(5, journalEntriesCount, positionCount);
        assertEquals(csvTradeProcessor.getDlQueueSize(), 2);
        cleanUp(connection);

    }

    private void cleanUp(Connection connection) throws SQLException {
        assert connection != null;
        HelperClass helperClass = new HelperClass(connection);
        helperClass.clearTable("trade_payloads");
        helperClass.clearTable( "journal_entries");
        helperClass.clearTable( "positions");
    }

}
