package io.reactivestax.contract.repository;

import io.reactivestax.domain.Trade;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public interface PayloadRepository {
    void updateLookUpStatus(String tradeId) throws SQLException, FileNotFoundException;


    void insertTradeIntoTradePayloadTable(String payload) throws Exception;

     String readTradePayloadByTradeId(String tradeId) throws FileNotFoundException, SQLException;
}