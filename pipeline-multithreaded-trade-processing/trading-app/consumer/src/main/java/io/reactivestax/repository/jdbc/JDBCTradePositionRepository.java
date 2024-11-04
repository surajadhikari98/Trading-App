package io.reactivestax.repository.jdbc;

import io.reactivestax.types.contract.repository.PositionRepository;
import io.reactivestax.types.dto.Trade;
import io.reactivestax.types.exception.OptimisticLockingException;
import io.reactivestax.utility.DBUtils;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCTradePositionRepository implements PositionRepository {


    private static JDBCTradePositionRepository instance;

    private JDBCTradePositionRepository() {}

    public static synchronized JDBCTradePositionRepository getInstance() {
        if (instance == null) {
            instance = new JDBCTradePositionRepository();
        }
        return instance;
    }


    @Override
    public Integer getCusipVersion(Trade trade) throws SQLException, FileNotFoundException {
        Connection connection = DBUtils.getInstance().getConnection();
        String query = "SELECT version FROM positions WHERE account_number = ? AND cusip = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, trade.getAccountNumber());
            stmt.setString(2, trade.getCusip());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("version");
            } else {
                return null;
            }
        }
    }

    @Override
    public boolean insertPosition(Trade trade) throws SQLException, FileNotFoundException {
        Connection connection = DBUtils.getInstance().getConnection();
        String insertQuery = "INSERT INTO positions (account_number, cusip, position, version) VALUES (?,?, ?, 0)";
        try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
            connection.setAutoCommit(false);
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

    // Update the position using optimistic locking
    @Override
    public boolean updatePosition(Trade trade, int version) throws SQLException, FileNotFoundException {
        int rowsUpdated = 0;
        Connection connection = DBUtils.getInstance().getConnection();
        String positionQuery = "SELECT position FROM positions where account_number = ? AND cusip = ?";
        String updateQuery = "UPDATE positions SET position = ?, version = version + 1 WHERE account_number = ? AND cusip = ? AND version = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery);
             PreparedStatement positionStatement = connection.prepareStatement(positionQuery)) {
            connection.setAutoCommit(false);
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

    public Integer getPositionCount() throws Exception {
        Connection connection = DBUtils.getInstance().getConnection();
        String insertQuery = "SELECT count(*) FROM positions";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }
    }
}
