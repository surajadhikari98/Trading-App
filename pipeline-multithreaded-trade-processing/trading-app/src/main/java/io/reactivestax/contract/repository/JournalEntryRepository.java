package io.reactivestax.contract.repository;

import io.reactivestax.model.Trade;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public interface JournalEntryRepository {

    void saveJournalEntry(Trade trade) throws SQLException, FileNotFoundException;

    void updateJournalStatus(String tradeId) throws SQLException, FileNotFoundException;

}
