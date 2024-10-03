package io.reactivestax.contract;

import io.reactivestax.domain.Trade;


public interface TradeProcessor {
//    void processTrades() throws InterruptedException, SQLException;
    void processTrade() throws Exception;
    void saveJournalEntry(Trade trade) throws Exception;
    boolean lookUpSecurityIdByCUSIP(String cusip) throws Exception;
    void processPosition(Trade trade);
}
