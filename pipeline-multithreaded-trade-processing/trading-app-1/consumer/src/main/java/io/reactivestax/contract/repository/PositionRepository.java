package io.reactivestax.contract.repository;

import io.reactivestax.dto.Trade;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public interface PositionRepository {
    Integer getCusipVersion(Trade trade) throws SQLException, FileNotFoundException;

    boolean insertPosition(Trade trade) throws SQLException, FileNotFoundException;

    boolean updatePosition(Trade trade, int version) throws Exception;
}
