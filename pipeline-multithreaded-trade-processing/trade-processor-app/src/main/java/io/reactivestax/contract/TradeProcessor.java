package io.reactivestax.contract;

import io.reactivestax.domain.Trade;

import java.sql.SQLException;

public interface TradeProcessor {
    void processTrade() throws Exception;
}
