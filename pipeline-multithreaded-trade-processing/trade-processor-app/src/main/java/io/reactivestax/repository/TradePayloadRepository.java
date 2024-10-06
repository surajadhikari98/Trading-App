package io.reactivestax.repository;

import io.reactivestax.contract.repository.PayloadRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static io.reactivestax.utils.Utility.checkValidity;


public class TradePayloadRepository implements PayloadRepository {
    private final Connection connection;

    public TradePayloadRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void updateLookUpStatus(String tradeId) throws SQLException {
        String updateQuery = "UPDATE trade_payloads SET lookup_status  = ? WHERE trade_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, "pass");
            stmt.setString(2, tradeId);
            stmt.executeUpdate();
            System.out.println("lookup updated suceesfully for: " + tradeId);
        }
    }

    @Override
    public void updateJournalStatus(String tradeId) throws SQLException {
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
}
