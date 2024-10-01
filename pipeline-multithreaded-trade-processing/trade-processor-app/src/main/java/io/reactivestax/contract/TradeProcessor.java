package io.reactivestax.contract;

import java.sql.SQLException;

public interface TradeProcessor {
    void processTrades() throws InterruptedException, SQLException;
}
