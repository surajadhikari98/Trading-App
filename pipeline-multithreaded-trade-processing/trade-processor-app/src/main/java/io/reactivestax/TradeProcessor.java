package io.reactivestax;

import java.sql.SQLException;

public interface TradeProcessor {
    void processTrades() throws InterruptedException, SQLException;
}
