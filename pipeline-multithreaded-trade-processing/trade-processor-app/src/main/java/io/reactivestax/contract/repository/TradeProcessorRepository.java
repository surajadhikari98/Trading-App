package io.reactivestax.contract.repository;

import io.reactivestax.domain.Trade;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public interface TradeProcessorRepository {

    void saveJournalEntry(Trade trade) throws SQLException, FileNotFoundException;

    boolean lookUpSecurityByCUSIP(String cusip) throws SQLException, FileNotFoundException;

}
