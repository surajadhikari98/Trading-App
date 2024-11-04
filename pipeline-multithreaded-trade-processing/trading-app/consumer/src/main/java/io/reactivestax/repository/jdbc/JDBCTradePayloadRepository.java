package io.reactivestax.repository.jdbc;

import io.reactivestax.types.contract.repository.PayloadRepository;
import io.reactivestax.types.enums.LookUpStatusEnum;
import io.reactivestax.types.enums.PostedStatusEnum;
import io.reactivestax.types.dto.Trade;
import io.reactivestax.types.enums.StatusReasonEnum;
import io.reactivestax.types.enums.ValidityStatusEnum;
import io.reactivestax.utility.DBUtils;

import java.io.FileNotFoundException;
import java.sql.*;

import static io.reactivestax.utility.Utility.prepareTrade;


public class JDBCTradePayloadRepository implements PayloadRepository {
    private static JDBCTradePayloadRepository instance;

    private JDBCTradePayloadRepository() {
    }

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
            stmt.setString(1, String.valueOf(LookUpStatusEnum.PASS));
            stmt.setString(2, tradeId);
            stmt.executeUpdate();
            System.out.println("lookup updated successfully for: " + tradeId);
        }
    }


    @Override
    public void updateJournalStatus(String tradeId) throws SQLException, FileNotFoundException {
        Connection connection = DBUtils.getInstance().getConnection();
        String updateQuery = "UPDATE trade_payloads SET je_status  = ? WHERE trade_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, String.valueOf(PostedStatusEnum.POSTED));
            stmt.setString(2, tradeId);
            stmt.executeUpdate();
        }
    }


    @Override
    public void insertTradeIntoTradePayloadTable(String payload) throws Exception {
        Connection connection = DBUtils.getInstance().getConnection();
        String insertQuery = "INSERT INTO trade_payloads (trade_id, validity_status, status_reason, lookup_status, je_status, payload) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            Trade trade = prepareTrade(payload);
            statement.setString(1, trade.getTradeIdentifier());
            statement.setString(2, String.valueOf(payload != null ? ValidityStatusEnum.VALID : ValidityStatusEnum.INVALID));
            statement.setString(3, payload != null ? String.valueOf(StatusReasonEnum.ALL_FIELDS_PRESENT) : String.valueOf(StatusReasonEnum.FIELDS_MISSING));
            statement.setString(4, "fail");
            statement.setString(5, "not_posted");
            statement.setString(6, payload);
            statement.executeUpdate();
        }
    }

    @Override
    public String readTradePayloadByTradeId(String tradeId) throws FileNotFoundException, SQLException {
        String insertQuery = "SELECT payload FROM trade_payloads WHERE trade_id = ?";
        Connection connection = DBUtils.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setString(1, tradeId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) return resultSet.getString(1);
        }
        return "";
    }

    public Integer selectTradePayload() throws Exception {
        Connection connection = DBUtils.getInstance().getConnection();
        String insertQuery = "SELECT count(*) FROM trade_payloads";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }
    }

}
