package io.reactivestax.repository;

import io.reactivestax.contract.repository.TradeProcessorRepository;
import io.reactivestax.domain.Trade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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


}
