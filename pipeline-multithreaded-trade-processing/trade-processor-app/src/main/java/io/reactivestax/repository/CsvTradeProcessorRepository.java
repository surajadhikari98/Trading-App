package io.reactivestax.repository;

import io.reactivestax.contract.repository.TradeProcessorRepository;
import io.reactivestax.domain.Trade;

import java.sql.*;

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
    public boolean lookUpSecurityByCUSIP(String cusip) throws SQLException {
        String lookupQueryForSecurity = "SELECT 1 FROM securities_reference WHERE cusip = ?";
        try(PreparedStatement lookUpStatement = connection.prepareStatement(lookupQueryForSecurity);) {
            lookUpStatement.setString(1, cusip);
            return lookUpStatement.executeQuery().next();
        }
    }

    public String callStoredProcedureForJournalAndPositionUpdate(Trade trade) throws Exception {
        String sql = "CALL insert_journal_and_position(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (CallableStatement stmt = connection.prepareCall(sql)) {
            // Step 4: Set the input parameters
            stmt.setString(1,trade.getTradeIdentifier());        // p_trade_id
            stmt.setString(2, trade.getTradeDateTime());          // p_trade_date
            stmt.setString(3, trade.getAccountNumber());    // p_account_number
            stmt.setString(4, trade.getCusip());                // p_cusip
            stmt.setString(5, trade.getDirection());                 // p_direction
            stmt.setInt(6, trade.getQuantity());                      // p_quantity
            stmt.setDouble(7, trade.getPrice());                // p_price
            stmt.setTimestamp(8, Timestamp.valueOf(trade.getTradeDateTime())); // p_posted_date
            stmt.registerOutParameter(9, Types.VARCHAR);
            stmt.execute();
            return  stmt.getString(9);
        }
    }

    public Integer getJournalEntriesCount() throws Exception {
        String insertQuery = "SELECT count(*) FROM journal_entries";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return  resultSet.getInt(1);
            }
            return 0;
        }
    }


}
