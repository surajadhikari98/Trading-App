package io.reactivestax.component;

import com.zaxxer.hikari.HikariDataSource;
import io.reactivestax.contract.TradeProcessor;
import io.reactivestax.domain.Trade;
import io.reactivestax.exception.OptimisticLockingException;
import io.reactivestax.hikari.DataSource;
import io.reactivestax.repository.TradePayloadRepository;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static io.reactivestax.repository.PositionRepository.*;

public class CsvTradeProcessor implements Runnable, TradeProcessor {
    public LinkedBlockingDeque<String> dequeue;
    public LinkedBlockingDeque<String> dlQueue;
    static Connection connection;
    Map<String, Integer> retryMapper = new ConcurrentHashMap<>();
    private final HikariDataSource dataSource = DataSource.getDataSource();

    static {
        try {
            connection = DataSource.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
                    if (!lookUpSecurityIdByCUSIP(trade.getCusip())) {
                        System.out.println("No security found....");
                        continue;
                    }
                    //issue is here check for it......
                    TradePayloadRepository.updateLookUpStatus(connection, tradeId);
                    boolean isPositionUpdated = processPosition(trade);
                    if (isPositionUpdated) {
                        saveJournalEntry(trade);
                    TradePayloadRepository.updateJournalStatus(connection, tradeId);
                    }
                }
            }
        }
        return tradeId;
    }

    @Override
    public void saveJournalEntry(Trade trade) throws SQLException {
        String insertQuery = "INSERT INTO journal_entries (trade_id, trade_date, account_number,cusip,direction, quantity, price) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            insertStatement.setString(1, trade.getTradeIdentifier());
            insertStatement.setString(2, trade.getTradeDateTime());
            insertStatement.setString(3, trade.getAccountNumber());
            insertStatement.setString(4, trade.getCusip());
            insertStatement.setString(5, trade.getDirection());
            insertStatement.setInt(6, trade.getQuantity());
            insertStatement.setDouble(7, trade.getPrice());
            insertStatement.executeUpdate();
        }
    }

    @Override
    public boolean lookUpSecurityIdByCUSIP(String cusip) throws SQLException {
        String lookupQueryForSecurity = "SELECT 1 FROM securities_reference WHERE cusip = ?";
       try(PreparedStatement lookUpStatement = connection.prepareStatement(lookupQueryForSecurity);) {
           lookUpStatement.setString(1, cusip);
           return lookUpStatement.executeQuery().next();
       }
    }


    // Process each position with optimistic locking and retry logic
    public boolean processPosition(Trade trade) {
        boolean isPositionUpdated = false;
        try (Connection connection = dataSource.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                int version = getCusipVersion(connection, trade);
                if (version == -1) {
                    isPositionUpdated = insertPosition(connection, trade);
                } else {
                   isPositionUpdated = updatePosition(connection, trade, version);
                }
            } catch (OptimisticLockingException e) {
                System.err.println(e.getMessage() + trade.getPosition());
                //logic for the retry count
                if (mappingForRetryCount(trade) < 3) {
                    this.dequeue.addLast(trade.getTradeIdentifier());
                } else {
                    dlQueue.put(trade.getTradeIdentifier());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
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
