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
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, trade.accountNumber());
        stmt.setString(2, trade.cusip());
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("version");
        } else {
            return -1;
        }
    }

    public static void insertPosition(Connection connection, Trade trade) throws SQLException {
        connection.setAutoCommit(false);
        String insertQuery = "INSERT INTO positions (account_number, cusip, position, version) VALUES (?,?, ?, 0)";
        PreparedStatement stmt = connection.prepareStatement(insertQuery);
        stmt.setString(1, trade.accountNumber());
        stmt.setString(2, trade.cusip());
        stmt.setDouble(3, trade.quantity());
        stmt.executeUpdate();
        System.out.println("New position for " + trade.accountNumber() + "is: " + trade.position());
        connection.commit();
        connection.setAutoCommit(true);
    }

    // Update the account balance using optimistic locking
    public static void updatePosition(Connection connection, Trade trade, int version) throws SQLException {
        connection.setAutoCommit(false);
        String positionQuery = "SELECT position FROM positions where account_number = ? AND cusip = ?";
        String updateQuery = "UPDATE positions SET position = ?, version = version + 1 WHERE account_number = ? AND cusip = ? AND version = ?";
        PreparedStatement stmt = connection.prepareStatement(updateQuery);
        PreparedStatement positionStatement = connection.prepareStatement(positionQuery);
        positionStatement.setString(1, trade.accountNumber());
        positionStatement.setString(2, trade.cusip());
        ResultSet resultSet = positionStatement.executeQuery();
        if (resultSet.next()) {
            if (trade.direction().equalsIgnoreCase("BUY")) {
                stmt.setDouble(1, resultSet.getInt(1) + trade.position());
            } else {
                stmt.setDouble(1, resultSet.getInt(1) - trade.position());
            }
            stmt.setString(2, trade.accountNumber());
            stmt.setString(3, trade.cusip());
            stmt.setInt(4, version);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                connection.rollback();
                throw new OptimisticLockingException("Optimistic locking failed, retrying transaction...");
            }
            connection.commit();
            System.out.println("Position updated for " + trade.cusip() + trade.position());
            connection.setAutoCommit(true);
        }
    }
}
