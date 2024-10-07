package io.reactivestax.repository;

import io.reactivestax.contract.repository.TradeProcessorRepository;
import io.reactivestax.domain.Trade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class CsvTradeProcessorRepository implements TradeProcessorRepository {
    private final Connection connection;
    public CsvTradeProcessorRepository(Connection connection) {
        this.connection = connection;
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

    public void callStoredProcedureForJournalAndPositionUpdate(Trade trade) throws Exception {
        String sql = "CALL insert_journal_and_position(?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Step 4: Set the input parameters
            stmt.setString(1,trade.getTradeIdentifier());        // p_trade_id
            stmt.setString(2, trade.getTradeDateTime());          // p_trade_date
            stmt.setString(3, trade.getAccountNumber());    // p_account_number
            stmt.setString(4, trade.getCusip());                // p_cusip
            stmt.setString(5, trade.getDirection());                 // p_direction
            stmt.setInt(6, get);                      // p_quantity
            stmt.setDouble(7, 150.75);                // p_price
            stmt.setTimestamp(8, Timestamp.valueOf("2024-10-06 12:34:56")); // p_posted_date
            statement.executeUpdate();
        }
    }


}
