package io.reactivestax.repository.jdbc;

import io.reactivestax.contract.repository.TradeProcessorRepository;
import io.reactivestax.domain.Trade;
import io.reactivestax.utils.DBUtils;

import java.io.FileNotFoundException;
import java.sql.*;

public class CsvTradeProcessorRepository implements TradeProcessorRepository {



    private static CsvTradeProcessorRepository instance;

    private CsvTradeProcessorRepository() {
    }

    public static synchronized CsvTradeProcessorRepository getInstance(){
        if(instance == null){
            instance = new CsvTradeProcessorRepository();
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

   @Override
    public boolean lookUpSecurityByCUSIP(String cusip) throws SQLException, FileNotFoundException {
       Connection connection = DBUtils.getInstance().getConnection();
        String lookupQueryForSecurity = "SELECT 1 FROM securities_reference WHERE cusip = ?";
       assert connection != null;
       try(PreparedStatement lookUpStatement = connection.prepareStatement(lookupQueryForSecurity);) {
            lookUpStatement.setString(1, cusip);
            return lookUpStatement.executeQuery().next();
        }
    }

    public String callStoredProcedureForJournalAndPositionUpdate(Trade trade) throws Exception {
        String sql = "CALL insert_journal_and_position(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = DBUtils.getInstance().getConnection();
        assert connection != null;
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
        Connection connection = DBUtils.getInstance().getConnection();
        String insertQuery = "SELECT count(*) FROM journal_entries";
        assert connection != null;
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return  resultSet.getInt(1);
            }
            return 0;
        }
    }


}
