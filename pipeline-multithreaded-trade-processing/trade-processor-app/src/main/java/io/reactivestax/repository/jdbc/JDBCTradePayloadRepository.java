package io.reactivestax.repository.jdbc;

import io.reactivestax.contract.repository.PayloadRepository;
import io.reactivestax.domain.Trade;
import io.reactivestax.enums.StatusReasonEnum;
import io.reactivestax.enums.ValidityStatusEnum;
import io.reactivestax.utils.DBUtils;

import java.io.FileNotFoundException;
import java.sql.*;


public class JDBCTradePayloadRepository implements PayloadRepository {
    private static JDBCTradePayloadRepository instance;

    private JDBCTradePayloadRepository(){}

     public static JDBCTradePayloadRepository getInstance() {
         if (instance == null) {
             instance = new JDBCTradePayloadRepository();
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
    public void insertTradeIntoTradePayloadTable(Trade payload) throws Exception {
        Connection connection = DBUtils.getInstance().getConnection();
        String insertQuery = "INSERT INTO trade_payloads (trade_id, validity_status, status_reason, lookup_status, je_status, payload) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setString(1, payload.getTradeIdentifier());
            statement.setString(2, String.valueOf(payload!= null ? ValidityStatusEnum.VALID : ValidityStatusEnum.INVALID));
            statement.setString(3, payload!= null ? String.valueOf(StatusReasonEnum.ALL_FIELDS_PRESENT) : String.valueOf(StatusReasonEnum.FIELDS_MISSING));
            statement.setString(4, "fail");
            statement.setString(5, "not_posted");
            statement.setString(6, String.valueOf(payload));
            statement.executeUpdate();
        }
    }

    @Override
    public String readTradePayloadByTradeId(String tradeId) {
        return "";
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
