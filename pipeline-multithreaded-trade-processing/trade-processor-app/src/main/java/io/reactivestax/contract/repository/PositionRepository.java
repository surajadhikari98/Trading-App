package io.reactivestax.contract.repository;

import io.reactivestax.domain.Trade;

import java.sql.SQLException;

public interface PositionRepository {
    int getCusipVersion(Trade trade) throws SQLException;

    boolean insertPosition(Trade trade) throws SQLException;

    boolean updatePosition(Trade trade, int version) throws SQLException;
}
