package io.reactivestax.contract.repository;

import io.reactivestax.domain.Trade;

import java.sql.Connection;
import java.sql.SQLException;

public interface PositionRepository {
      int getCusipVersion(Connection connection, Trade trade) throws SQLException;
     void insertPosition(Connection connection, Trade trade) throws SQLException;
    void updatePosition(Connection connection, Trade trade, int version) throws SQLException;
}
