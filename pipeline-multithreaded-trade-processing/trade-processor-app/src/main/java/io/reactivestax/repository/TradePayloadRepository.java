package io.reactivestax.repository;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TradePayloadRepository {
    public static void updateLookUpStatus(Connection connection, String tradeId) throws SQLException {
//        String selectQuery = "SELECT position FROM positions where account_number = ? AND cusip = ?";
        String updateQuery = "UPDATE trade_payloads SET lookup_status  = ? WHERE trade_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, "pass");
            stmt.setString(2, tradeId);
            stmt.executeUpdate();
            System.out.println("lookup updated suceesfully for: " + tradeId);
        }
    }

    public static void updateJournalStatus(Connection connection, String tradeId) throws SQLException {
        String updateQuery = "UPDATE trade_payloads SET je_status  = ? WHERE trade_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, "posted");
            stmt.setString(2, tradeId);
            stmt.executeUpdate();
            System.out.println("Journal Status updated suceesfully for: " + tradeId);

        }
    }
}
