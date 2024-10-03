package io.reactivestax.repository;

import io.reactivestax.contract.repository.TradeProcessorRepository;
import io.reactivestax.domain.Trade;
import io.reactivestax.hikari.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class CsvTradeProcessorRepository implements TradeProcessorRepository {
    @Override
    public void saveJournalEntry(Trade trade) throws Exception {
        String insertQuery = "INSERT INTO journal_entries (trade_id, trade_date, account_number,cusip,direction, quantity, price) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(Connection connection = DataSource.getConnection();
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            insertStatement.setString(1, trade.tradeIdentifier());
            insertStatement.setString(2, trade.tradeDateTime());
            insertStatement.setString(3, trade.accountNumber());
            insertStatement.setString(4, trade.cusip());
            insertStatement.setString(5, trade.direction());
            insertStatement.setInt(6, trade.quantity());
            insertStatement.setDouble(7, trade.price());
            insertStatement.executeUpdate();
        }
    }

    @Override
    public boolean lookUpSecurityIdByCUSIP(String cusip) throws Exception {
        String lookupQueryForSecurity = "SELECT 1 FROM securities_reference WHERE cusip = ?";
        try(PreparedStatement lookUpStatement = DataSource.getConnection().prepareStatement(lookupQueryForSecurity);) {
            lookUpStatement.setString(1, cusip);
            return lookUpStatement.executeQuery().next();
        }
    }


}
