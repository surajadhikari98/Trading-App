package io.reactivestax.contract.repository;

import io.reactivestax.domain.Trade;

public interface TradeProcessorRepository {
    void saveJournalEntry(Trade trade) throws Exception;
    boolean lookUpSecurityIdByCUSIP(String cusip) throws Exception;

}
