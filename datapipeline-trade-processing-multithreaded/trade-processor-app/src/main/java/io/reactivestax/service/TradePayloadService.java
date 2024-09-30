package io.reactivestax.service;

import io.reactivestax.domain.TradePayload;

public interface TradePayloadService {
    TradePayload readTradePayloadByTradeId(String trade_id);

    Boolean insertTradeIntoTradePayloadTable(String trade_id, String status, String payload);

}
