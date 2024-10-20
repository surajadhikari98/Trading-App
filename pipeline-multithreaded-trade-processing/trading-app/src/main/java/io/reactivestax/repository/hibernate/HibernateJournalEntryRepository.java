package io.reactivestax.repository.hibernate;

import io.reactivestax.contract.repository.JournalEntryRepository;
import io.reactivestax.domain.Trade;
import io.reactivestax.entity.JournalEntries;
import io.reactivestax.entity.TradePayload;
import io.reactivestax.enums.PostedStatusEnum;
import io.reactivestax.utils.DBUtils;
import io.reactivestax.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class HibernateJournalEntryRepository implements JournalEntryRepository {

    private static HibernateJournalEntryRepository instance;

    private HibernateJournalEntryRepository() {
    }

    public static synchronized HibernateJournalEntryRepository getInstance() {
        if (instance == null) {
            instance = new HibernateJournalEntryRepository();
        }
        return instance;
    }

    @Override
    public void saveJournalEntry(Trade trade) {
      Session session = HibernateUtil.getInstance().getConnection();
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                JournalEntries journalEntries = getJournalEntries(trade);
                session.persist(journalEntries);
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                    System.out.println(e.getMessage());
                }
            }
    }

    private static JournalEntries getJournalEntries(Trade trade) {
        JournalEntries journalEntries = new JournalEntries();
        journalEntries.setTradeId(trade.getTradeIdentifier());
        journalEntries.setTradeDate(trade.getTradeDateTime());
        journalEntries.setAccountNumber(trade.getAccountNumber());
        journalEntries.setCusip(trade.getCusip());
        journalEntries.setDirection(trade.getDirection());
        journalEntries.setQuantity(trade.getQuantity());
        journalEntries.setPrice(trade.getPrice());
        return journalEntries;
    }


    @Override
    public void updateJournalStatus(String tradeId) {
       Session session = HibernateUtil.getInstance().getConnection();
            session.beginTransaction();
            TradePayload tradePayload = session.get(TradePayload.class, tradeId);
            tradePayload.setJeStatus(String.valueOf(PostedStatusEnum.POSTED));
            session.getTransaction().commit();
        }
}

