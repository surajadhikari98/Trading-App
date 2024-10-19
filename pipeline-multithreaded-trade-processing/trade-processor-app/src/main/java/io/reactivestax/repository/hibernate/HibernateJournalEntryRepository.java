package io.reactivestax.repository.hibernate;

import io.reactivestax.domain.Trade;
import io.reactivestax.entity.JournalEntries;
import io.reactivestax.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;


public class HibernateJournalEntryRepository {

    private static HibernateJournalEntryRepository instance;

    private HibernateJournalEntryRepository(){}

    public static synchronized HibernateJournalEntryRepository getInstance(){
        if(instance == null) {
            instance = new HibernateJournalEntryRepository();
        }
        return instance;
    }

    public void persistJournalEntry(Trade trade) {
        try (Session session = HibernateUtil.getInstance().getSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                JournalEntries journalEntries = new JournalEntries();
                journalEntries.setTradeId(trade.getTradeIdentifier());
                journalEntries.setTradeDate(trade.getTradeDateTime());
                journalEntries.setAccountNumber(trade.getAccountNumber());
                journalEntries.setCusip(trade.getCusip());
                journalEntries.setDirection(trade.getDirection());
                journalEntries.setQuantity(trade.getQuantity());
                journalEntries.setPrice(trade.getPrice());

                session.persist(journalEntries);
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                    System.out.println(e.getMessage());
                }
            }
        }
    }

}

