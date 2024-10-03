package io.reactivestax.component;

import com.zaxxer.hikari.HikariDataSource;
import io.reactivestax.contract.TradeProcessor;
import io.reactivestax.domain.Trade;
import io.reactivestax.exception.OptimisticLockingException;
import io.reactivestax.hikari.DataSource;
import io.reactivestax.repository.CsvTradeProcessorRepository;
import io.reactivestax.repository.TradePositionRepository;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;


@SuppressWarnings("java:S106")
public class CsvTradeProcessor implements Runnable, TradeProcessor {
    public LinkedBlockingDeque<String> dequeue;
    public LinkedBlockingDeque<String> dlQueue;
    Map<String, Integer> retryMapper = new ConcurrentHashMap<>();
    private final HikariDataSource dataSource = DataSource.getDataSource();

    public CsvTradeProcessor(LinkedBlockingDeque<String> dequeue) throws Exception {
        this.dequeue = dequeue;
    }

    @Override
    public void run() {
        try {
            String tradeIdentifier = processTrade();
            System.out.println("Successful insertion for the trade id : " + tradeIdentifier);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String processTrade() throws Exception {
        CsvTradeProcessorRepository csvTradeProcessorRepository = new CsvTradeProcessorRepository();
        String tradeId = "";
        while (!this.dequeue.isEmpty()) {
            tradeId = this.dequeue.take();
            String lookupQuery = "SELECT payload FROM trade_payloads WHERE trade_id = ?";
            try (Connection connection = DataSource.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(lookupQuery)) {
                stmt.setString(1, tradeId);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    String payload = resultSet.getString(1);
                    String[] payloads = payload.split(",");
                    Trade trade = new Trade(payloads[0], payloads[1], payloads[2], payloads[3], payloads[4], Integer.parseInt(payloads[5]), Double.parseDouble(payloads[6]), Integer.parseInt(payloads[5]));
                    System.out.println("result journal" + payload);
                    if (!csvTradeProcessorRepository.lookUpSecurityIdByCUSIP(trade.cusip())) {
                        System.out.println("No security found....");
                        continue;
                    }
                    csvTradeProcessorRepository.saveJournalEntry(trade);
                    processPosition(trade);
                }
            }
        }
        return tradeId;
    }


    // Process each position with optimistic locking and retry logic
    public void processPosition(Trade trade) {
        TradePositionRepository tradePositionRepository = new TradePositionRepository();
        try (Connection connection = dataSource.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                int version = tradePositionRepository.getCusipVersion(connection, trade);

                if (version == -1) {
                    tradePositionRepository.insertPosition(connection, trade);
                } else {
                    tradePositionRepository.updatePosition(connection, trade, version);
                }
            } catch (OptimisticLockingException e) {
                System.err.println(e.getMessage() + trade.position());
                //logic for the retry count
                if (mappingForRetryCount(trade) < 3) {
                    this.dequeue.addLast(trade.tradeIdentifier());
                } else {
                    dlQueue.put(trade.tradeIdentifier());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int mappingForRetryCount(Trade trade) {
        int errorCount;
        errorCount = retryMapper.putIfAbsent(trade.tradeIdentifier(), 1);
        if (retryMapper.get(trade.tradeIdentifier()) != null) {
            errorCount = retryMapper.compute(trade.tradeIdentifier(), (k, i) -> i + 1);
        }
        return errorCount;
    }
}
