package io.reactivestax.component;

import io.reactivestax.contract.TradeProcessor;
import io.reactivestax.domain.Trade;
import io.reactivestax.exception.OptimisticLockingException;
import io.reactivestax.hikari.DataSource;
import io.reactivestax.repository.TradePositionRepository;
import io.reactivestax.repository.TradePayloadRepository;
import io.reactivestax.repository.CsvTradeProcessorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;


public class CsvTradeProcessor implements Runnable, TradeProcessor {
    private final LinkedBlockingDeque<String> dequeue;
    private final LinkedBlockingDeque<String> dlQueue = new LinkedBlockingDeque<>();
    Map<String, Integer> retryMapper = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(CsvTradeProcessor.class);
    static AtomicInteger countSec = new AtomicInteger(0);


    public CsvTradeProcessor(LinkedBlockingDeque<String> dequeue){
        this.dequeue = dequeue;
    }

    @Override
    public void run() {
        try {
            processTrade();
        } catch (Exception e) {
            logger.error("trade processor {}", e.getMessage());
        }
    }

    @Override
    public void processTrade() throws Exception {
        CsvTradeProcessorRepository csvTradeProcessorRepository = new CsvTradeProcessorRepository(DataSource.getConnection());
        TradePayloadRepository tradePayloadRepository = new TradePayloadRepository(DataSource.getConnection());
        TradePositionRepository tradePositionRepository = new TradePositionRepository(DataSource.getConnection());
        Connection connection = DataSource.getConnection();

        while (!this.dequeue.isEmpty()) {
           String tradeId = this.dequeue.take();
            String lookupQuery = "SELECT payload FROM trade_payloads WHERE trade_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(lookupQuery)) {
                stmt.setString(1, tradeId);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    String payload = resultSet.getString(1);
                    String[] payloads = payload.split(",");
                    Trade trade = new Trade(payloads[0], payloads[1], payloads[2], payloads[3], payloads[4], Integer.parseInt(payloads[5]), Double.parseDouble(payloads[6]), Integer.parseInt(payloads[5]));
                    logger.info("result journal : {}", payload);
//                    System.out.println("result journal" +  payload);
                    if (!csvTradeProcessorRepository.lookUpSecurityByCUSIP(trade.getCusip())) {
                        logger.info("No security found....");
//                        System.out.println("no sec found" + trade.getCusip() + dlQueue.size());
                        dlQueue.put(trade.getTradeIdentifier());
                        System.out.println("timess" + trade.getCusip() + countSec.incrementAndGet());
                        continue;
                    }
                    String tradeIdentifier = csvTradeProcessorRepository.callStoredProcedureForJournalAndPositionUpdate(trade);
                    if(tradeIdentifier == null) {
                        logger.error("Optimistic locking occurred with trade: {}", trade.getPosition());
                        //logic for the retry count
                        if (mappingForRetryCount(trade) < 3) {
                            this.dequeue.addLast(trade.getTradeIdentifier());
                        } else {
                            dlQueue.put(trade.getTradeIdentifier());
                        }
                    } else{
                        logger.info("Successful insertion for the trade with trade id: {}", tradeIdentifier);
                    }
//                    tradePayloadRepository.updateLookUpStatus(tradeId);
//                        csvTradeProcessorRepository.saveJournalEntry(trade);
//                        tradePayloadRepository.updateJournalStatus(tradeId);
                    }
                }
            }
    }


    // Process each position with optimistic locking and retry logic
    public boolean processPosition(TradePositionRepository tradePositionRepository, Trade trade) throws SQLException, InterruptedException {
        boolean isPositionUpdated = false;
            try {
                int version = tradePositionRepository.getCusipVersion(trade);
                if (version == -1) {
                    isPositionUpdated = tradePositionRepository.insertPosition(trade);
                } else {
                    isPositionUpdated = tradePositionRepository.updatePosition(trade, version);
                }
            } catch (OptimisticLockingException e) {
                logger.error("Optimistic locking occurred: {} with position: {}", e.getMessage(), trade.getPosition());
                //logic for the retry count
                if (mappingForRetryCount(trade) < 3) {
                    this.dequeue.addLast(trade.getTradeIdentifier());
                } else {
                    dlQueue.put(trade.getTradeIdentifier());
                }
            }
        return isPositionUpdated;
    }

    public int mappingForRetryCount(Trade trade) {
        int errorCount;
        errorCount = retryMapper.putIfAbsent(trade.getTradeIdentifier(), 1);
        if (retryMapper.get(trade.getTradeIdentifier()) != null) {
            errorCount = retryMapper.compute(trade.getTradeIdentifier(), (k, i) -> i + 1);
        }
        return errorCount;
    }
}
