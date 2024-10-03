package io.reactivestax.contract;

import io.reactivestax.domain.Trade;

import java.sql.SQLException;

public interface TradeProcessor {
//    void processTrades() throws InterruptedException, SQLException;
    String processTrade() throws Exception;
    void saveJournalEntry(Trade trade) throws SQLException;
    boolean lookUpSecurityIdByCUSIP(String cusip) throws SQLException;
    boolean processPosition(Trade trade);
}
