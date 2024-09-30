package io.reactivestax;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PositionRepository {
    // Get the current version of the account for optimistic locking
    public static int getCusipVersion(Connection connection, String cusip) throws SQLException {
        String query = "SELECT version FROM positions WHERE cusip = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, cusip);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("version");
        } else {
            return -1;
        }
    }

    // Insert a new account for a credit card number
    public static void insertPosition(Connection connection, JournalEntry journalEntry) throws SQLException {
        connection.setAutoCommit(false);
        String insertQuery = "INSERT INTO positions (account_number, cusip, position, version) VALUES (?,?, ?, 0)";
        PreparedStatement stmt = connection.prepareStatement(insertQuery);
        stmt.setString(1, journalEntry.getAccountNumber());
        stmt.setString(2, journalEntry.getCusip());
        stmt.setDouble(3,  journalEntry.getQuantity());
        stmt.executeUpdate();
        System.out.println("New position for " + journalEntry.getAccountNumber() + "is: " + journalEntry.getPosition());
        connection.commit();
        connection.setAutoCommit(true);
    }

    public static void insertBuyPosition(Connection connection, JournalEntry journalEntry) throws SQLException {
        connection.setAutoCommit(false);
        String insertQuery = "INSERT INTO positions (account_number, position, version) VALUES (?, ?, 0)";
        PreparedStatement stmt = connection.prepareStatement(insertQuery);
        stmt.setString(1, journalEntry.getAccountNumber());
        stmt.setDouble(2, journalEntry.getPosition() + journalEntry.getQuantity());
        stmt.executeUpdate();
        System.out.println("New position for " + journalEntry.getAccountNumber() + "is: " + journalEntry.getPosition());
        connection.commit();
        connection.setAutoCommit(true);
    }

    // Update the account balance using optimistic locking
    public static void updatePosition(Connection connection, JournalEntry journalEntry, int version) throws SQLException {
        connection.setAutoCommit(false);
        String positionQuery = "SELECT position FROM positions where cusip = ?";
        String updateQuery = "UPDATE positions SET position = ?, version = version + 1 WHERE cusip = ? AND version = ?";
        PreparedStatement stmt = connection.prepareStatement(updateQuery);
        PreparedStatement positionStatement = connection.prepareStatement(positionQuery);
        positionStatement.setString(1, journalEntry.getCusip());
        ResultSet resultSet = positionStatement.executeQuery();
        if(resultSet.next()) {
            if(journalEntry.getDirection().equalsIgnoreCase("BUY")){
            stmt.setDouble(1, resultSet.getInt(1) + journalEntry.getPosition());
            } else {
                stmt.setDouble(1, resultSet.getInt(1) - journalEntry.getPosition());
            }
            stmt.setString(2, journalEntry.getCusip());
            stmt.setInt(3, version);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                // Optimistic locking failed, retry
                connection.rollback();
                throw new OptimisticLockingException("Optimistic locking failed, retrying transaction...");
            }
            connection.commit();
            System.out.println("Position updated for " + journalEntry.getCusip() + journalEntry.getPosition());
            connection.setAutoCommit(true);
        }
    }
}
