package io.reactivestax.component;

import io.reactivestax.contract.TradeProcessor;
import io.reactivestax.domain.Trade;
import io.reactivestax.exception.OptimisticLockingException;
import io.reactivestax.hikari.DataSource;
import io.reactivestax.repository.TradePositionRepository;
import io.reactivestax.repository.TradePayloadRepository;
import io.reactivestax.repository.CsvTradeProcessorRepository;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;


public class CsvTradeProcessor implements Runnable, TradeProcessor {
    public LinkedBlockingDeque<String> dequeue;
    public LinkedBlockingDeque<String> dlQueue;
    Map<String, Integer> retryMapper = new ConcurrentHashMap<>();

    public CsvTradeProcessor(LinkedBlockingDeque<String> dequeue) throws Exception {
        this.dequeue = dequeue;
    }

    @Override
    public void run() {
        try {
            String tradeIdentifier = processTrade();
            System.out.println("Successful insertion for the trade id : " + tradeIdentifier);
        } catch (Exception e) {
            System.err.println("trade processor " + e.getMessage());
//            throw new RuntimeException(e);
        }
    }

    @Override
    public String processTrade() throws Exception {
        String tradeId = "";
        CsvTradeProcessorRepository csvTradeProcessorRepository = new CsvTradeProcessorRepository(DataSource.getConnection());
        TradePayloadRepository tradePayloadRepository = new TradePayloadRepository(DataSource.getConnection());
        TradePositionRepository tradePositionRepository = new TradePositionRepository(DataSource.getConnection());
        Connection connection = DataSource.getConnection();

        while (!this.dequeue.isEmpty()) {
            tradeId = this.dequeue.take();
            String lookupQuery = "SELECT payload FROM trade_payloads WHERE trade_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(lookupQuery)) {
                stmt.setString(1, tradeId);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    String payload = resultSet.getString(1);
                    String[] payloads = payload.split(",");
                    Trade trade = new Trade(payloads[0], payloads[1], payloads[2], payloads[3], payloads[4], Integer.parseInt(payloads[5]), Double.parseDouble(payloads[6]), Integer.parseInt(payloads[5]));
                    System.out.println("result journal" + payload);
                    if (!csvTradeProcessorRepository.lookUpSecurityIdByCUSIP(trade.getCusip())) {
                        System.out.println("No security found....");
                        continue;
                    }
                    tradePayloadRepository.updateLookUpStatus(tradeId);
                    boolean isPositionUpdated = processPosition(tradePositionRepository,trade);
                    if (isPositionUpdated) {
                        csvTradeProcessorRepository.saveJournalEntry(trade);
                        tradePayloadRepository.updateJournalStatus(tradeId);
                    }
                }
            }
        }
        return tradeId;
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
                System.err.println(e.getMessage() + trade.getPosition());
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
