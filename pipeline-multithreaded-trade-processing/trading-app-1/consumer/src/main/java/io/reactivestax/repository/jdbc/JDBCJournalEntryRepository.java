package io.reactivestax.repository.jdbc;

import io.reactivestax.types.contract.repository.JournalEntryRepository;
import io.reactivestax.types.dto.Trade;
import io.reactivestax.utility.DBUtils;

import java.io.FileNotFoundException;
import java.sql.*;

public class JDBCJournalEntryRepository implements JournalEntryRepository {


    private static JDBCJournalEntryRepository instance;

    private JDBCJournalEntryRepository() {
    }

    public static synchronized JDBCJournalEntryRepository getInstance() {
        if (instance == null) {
            instance = new JDBCJournalEntryRepository();
        }
        return instance;
    }


    @Override
    public void saveJournalEntry(Trade trade) throws SQLException, FileNotFoundException {
        Connection connection = DBUtils.getInstance().getConnection();
        String insertQuery = "INSERT INTO journal_entries (trade_id, trade_date, account_number,cusip,direction, quantity, price) VALUES (?, ?, ?, ?, ?, ?, ?)";
        assert connection != null;
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





    public String callStoredProcedureForJournalAndPositionUpdate(Trade trade) throws Exception {
        String sql = "CALL insert_journal_and_position(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = DBUtils.getInstance().getConnection();
        assert connection != null;
        try (CallableStatement stmt = connection.prepareCall(sql)) {
            stmt.setString(1, trade.getTradeIdentifier());
            stmt.setString(2, trade.getTradeDateTime());
            stmt.setString(3, trade.getAccountNumber());
            stmt.setString(4, trade.getCusip());
            stmt.setString(5, trade.getDirection());
            stmt.setInt(6, trade.getQuantity());
            stmt.setDouble(7, trade.getPrice());
            stmt.setTimestamp(8, Timestamp.valueOf(trade.getTradeDateTime()));
            stmt.registerOutParameter(9, Types.VARCHAR);
            stmt.execute();
            return stmt.getString(9);
        }
    }

    public Integer getJournalEntriesCount() throws Exception {
        Connection connection = DBUtils.getInstance().getConnection();
        String insertQuery = "SELECT count(*) FROM journal_entries";
        assert connection != null;
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }
    }
}
