package io.reactivestax.service;

import io.reactivestax.types.contract.QueueLoader;
import io.reactivestax.types.contract.TradeProcessor;
import io.reactivestax.types.contract.repository.JournalEntryRepository;
import io.reactivestax.types.contract.repository.PayloadRepository;
import io.reactivestax.types.contract.repository.PositionRepository;
import io.reactivestax.types.contract.repository.SecuritiesReferenceRepository;
import io.reactivestax.types.dto.Trade;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static io.reactivestax.factory.BeanFactory.*;
import static io.reactivestax.utility.Utility.prepareTrade;

@Slf4j
public class TradeProcessorService implements Callable<Void>, TradeProcessor {
    private static final AtomicInteger countSec = new AtomicInteger(0);
    private final String queueName;

    public TradeProcessorService(String queueName) {
        this.queueName = queueName;
    }


    @Override
    public Void call() {
        try {
            processTrade();
        } catch (Exception e) {
            TradeProcessorService.log.info("trade processor:  {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void processTrade() throws Exception {
        QueueLoader queueLoader = getQueueSetUp();
        assert queueLoader != null;
        queueLoader.consumeMessage(queueName);
    }

    public static void processJournalWithPosition(String tradeId) throws Exception {
        PayloadRepository tradePayloadRepository = getTradePayloadRepository();
        String payload = tradePayloadRepository.readTradePayloadByTradeId(tradeId);
        SecuritiesReferenceRepository lookupSecuritiesRepository = getLookupSecuritiesRepository();
        JournalEntryRepository journalEntryRepository = getJournalEntryRepository();
        Trade trade = prepareTrade(payload);
        log.info("result journal{}", payload);
        if (!lookupSecuritiesRepository.lookupSecurities(trade.getCusip())) {
            log.warn("No security found....");
            log.debug("times {} {}", trade.getCusip(), countSec.incrementAndGet());
            throw new SQLException(); // For checking the max retry mechanism throwing error and catching it in retry mechanism.....
        } else {
            journalEntryRepository.saveJournalEntry(trade);
            tradePayloadRepository.updateLookUpStatus(tradeId);
            tradePayloadRepository.updateJournalStatus(tradeId);
            processPosition(trade);
        }
    }

    public static void processPosition(Trade trade) throws Exception {
        PositionRepository positionsRepository = getPositionsRepository();
        Integer version = positionsRepository.getCusipVersion(trade);
        if (version != null) {
            positionsRepository.updatePosition(trade, version);
        } else {
            positionsRepository.insertPosition(trade);
        }
    }
}
