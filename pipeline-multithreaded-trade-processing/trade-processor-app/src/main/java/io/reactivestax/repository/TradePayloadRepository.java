package io.reactivestax.repository;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TradePayloadRepository {
    public static void updateLookUpStatus(Connection connection, String tradeId) throws SQLException {
        String updateQuery = "UPDATE trade_payloads SET lookup_status  = ? WHERE tradeId = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, "pass");
            stmt.executeUpdate();
        }
    }

    public static void updateJournalStatus(Connection connection, String tradeId) throws SQLException {
        String updateQuery = "UPDATE trade_payloads SET je_status  = ? WHERE tradeId = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, "posted");
            stmt.executeUpdate();
        }
    }
}
