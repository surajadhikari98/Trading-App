package io.reactivestax.contract.repository;

import io.reactivestax.domain.Trade;

import java.sql.SQLException;

public interface TradeProcessorRepository {

    void saveJournalEntry(Trade trade) throws SQLException;

    boolean lookUpSecurityIdByCUSIP(String cusip) throws SQLException;

}
