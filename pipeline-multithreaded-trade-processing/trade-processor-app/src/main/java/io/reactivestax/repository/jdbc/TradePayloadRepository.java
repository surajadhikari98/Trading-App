package io.reactivestax.repository.jdbc;

import io.reactivestax.contract.repository.PayloadRepository;
import io.reactivestax.utils.DBUtils;

import java.io.FileNotFoundException;
import java.sql.*;

import static io.reactivestax.utils.Utility.checkValidity;


public class TradePayloadRepository implements PayloadRepository {
    private static TradePayloadRepository instance;

    private TradePayloadRepository(){}

     public static TradePayloadRepository getInstance() {
         if (instance == null) {
             instance = new TradePayloadRepository();
         }
         return instance;
    }

    @Override
    public void updateLookUpStatus(String tradeId) throws SQLException, FileNotFoundException {
        Connection connection = DBUtils.getInstance().getConnection();
        String updateQuery = "UPDATE trade_payloads SET lookup_status  = ? WHERE trade_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, "pass");
            stmt.setString(2, tradeId);
            stmt.executeUpdate();
            System.out.println("lookup updated suceesfully for: " + tradeId);
        }
    }

    @Override
    public void updateJournalStatus(String tradeId) throws SQLException, FileNotFoundException {
        Connection connection = DBUtils.getInstance().getConnection();
        String updateQuery = "UPDATE trade_payloads SET je_status  = ? WHERE trade_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, "posted");
            stmt.setString(2, tradeId);
            stmt.executeUpdate();
            System.out.println("Journal Status updated suceesfully for: " + tradeId);
        }
    }

    @Override
    public void insertTradeIntoTradePayloadTable(String payload) throws Exception {
        Connection connection = DBUtils.getInstance().getConnection();
        String[] split = payload.split(",");
        String insertQuery = "INSERT INTO trade_payloads (trade_id, validity_status, status_reason, lookup_status, je_status, payload) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setString(1, split[0]);
            statement.setString(2, checkValidity(split) ? "valid" : "inValid");
            statement.setString(3, checkValidity(split) ? "All field present " : "Fields missing");
            statement.setString(4, "fail");
            statement.setString(5, "not_posted");
            statement.setString(6, payload);
            statement.executeUpdate();
        }
    }

    public Integer selectTradePayload() throws Exception {
        Connection connection = DBUtils.getInstance().getConnection();
        String insertQuery = "SELECT count(*) FROM trade_payloads";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return  resultSet.getInt(1);
            }
            return 0;
        }
    }

}
