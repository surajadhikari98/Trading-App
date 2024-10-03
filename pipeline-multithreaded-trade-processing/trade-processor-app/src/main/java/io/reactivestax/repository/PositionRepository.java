package io.reactivestax.repository;

import io.reactivestax.domain.Trade;
import io.reactivestax.exception.OptimisticLockingException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PositionRepository {
    public static int getCusipVersion(Connection connection, Trade trade) throws SQLException {
        String query = "SELECT version FROM positions WHERE account_number = ? AND cusip = ?";
        try(PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, trade.getAccountNumber());
            stmt.setString(2, trade.getCusip());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("version");
            } else {
                return -1;
            }
        }
    }

    public static boolean insertPosition(Connection connection, Trade trade) throws SQLException {
        connection.setAutoCommit(false);
        String insertQuery = "INSERT INTO positions (account_number, cusip, position, version) VALUES (?,?, ?, 0)";
        try(PreparedStatement stmt = connection.prepareStatement(insertQuery);) {
            stmt.setString(1, trade.getAccountNumber());
            stmt.setString(2, trade.getCusip());
            stmt.setDouble(3, trade.getQuantity());
            int i = stmt.executeUpdate();
            System.out.println("New position for " + trade.getAccountNumber() + "is: " + trade.getPosition());
            connection.commit();
            connection.setAutoCommit(true);
            return i > 0;
        }
    }

    // Update the account balance using optimistic locking
    public static boolean updatePosition(Connection connection, Trade trade, int version) throws SQLException {
        int rowsUpdated = 0;
        connection.setAutoCommit(false);
        String positionQuery = "SELECT position FROM positions where account_number = ? AND cusip = ?";
        String updateQuery = "UPDATE positions SET position = ?, version = version + 1 WHERE account_number = ? AND cusip = ? AND version = ?";
        try( PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            PreparedStatement positionStatement = connection.prepareStatement(positionQuery);
            positionStatement.setString(1, trade.getAccountNumber());
            positionStatement.setString(2, trade.getCusip());
            ResultSet resultSet = positionStatement.executeQuery();
            if (resultSet.next()) {
                if (trade.getDirection().equalsIgnoreCase("BUY")) {
                    stmt.setDouble(1, resultSet.getInt(1) + trade.getPosition());
                } else {
                    stmt.setDouble(1, resultSet.getInt(1) - trade.getPosition());
                }
                stmt.setString(2, trade.getAccountNumber());
                stmt.setString(3, trade.getCusip());
                stmt.setInt(4, version);

                rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    connection.rollback();
                    throw new OptimisticLockingException("Optimistic locking failed, retrying transaction...");
                }
                connection.commit();
                System.out.println("Position updated for " + trade.getCusip() + trade.getPosition());
                connection.setAutoCommit(true);
            }
        }
        return rowsUpdated > 0;
    }
}
