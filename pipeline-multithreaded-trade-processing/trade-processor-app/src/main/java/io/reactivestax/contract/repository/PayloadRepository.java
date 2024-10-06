package io.reactivestax.contract.repository;

import java.sql.SQLException;

public interface PayloadRepository {
    void updateLookUpStatus(String tradeId) throws SQLException;

    void updateJournalStatus(String tradeId) throws SQLException;

    void insertTradeIntoTradePayloadTable(String payload) throws Exception;
}