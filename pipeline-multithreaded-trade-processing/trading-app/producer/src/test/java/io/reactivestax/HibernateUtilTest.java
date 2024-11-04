package io.reactivestax;

import io.reactivestax.repository.hibernate.entity.TradePayload;
import io.reactivestax.types.enums.LookUpStatusEnum;
import io.reactivestax.types.enums.PostedStatusEnum;
import io.reactivestax.types.enums.ValidityStatusEnum;
import io.reactivestax.utility.database.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.Assert.*;

public class HibernateUtilTest {

    @BeforeAll
    public static void setUp() {
        HibernateUtil.setConfigResource("hibernate-h2.cfg.xml");
    }

    @Test
    public void testSessionFactoryAndConnection() {
        HibernateUtil instance = HibernateUtil.getInstance();
        Session session = instance.getConnection();
        assertNotNull("Session should not be null", session);
        session.close();
    }

    @Test
    public void testTransactionCommit() {
        HibernateUtil instance = HibernateUtil.getInstance();
        Session session = instance.getConnection();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            TradePayload tradePayload = new TradePayload();
            tradePayload.setTradeId("2");
            tradePayload.setValidityStatus(String.valueOf(ValidityStatusEnum.VALID));
            tradePayload.setStatusReason("All field present ");
            tradePayload.setLookupStatus(String.valueOf(LookUpStatusEnum.FAIL));
            tradePayload.setJeStatus(String.valueOf(PostedStatusEnum.NOT_POSTED));
            tradePayload.setPayload("");
            session.persist(tradePayload);
            transaction.commit();
            assertTrue(session.contains("Trade payload should be in session", tradePayload));
            TradePayload retrievedTradePayload = session.createQuery("FROM TradePayload WHERE tradeId = :tradeId", TradePayload.class)
                    .setParameter("tradeId", "2")
                    .uniqueResult();
            assertNotNull(retrievedTradePayload);
            assertEquals("trade payload and retrieved value should be equal", tradePayload, retrievedTradePayload);
        } catch (Exception e) {
            transaction.rollback();
        }
    }

    @Test
    public void testTransactionRollback() {
        HibernateUtil instance = HibernateUtil.getInstance();
        Session session = instance.getConnection();
        session.getTransaction().begin();
        TradePayload tradePayload = new TradePayload();
        session.getTransaction().rollback();
        TradePayload retrievedTrade = session.get(TradePayload.class, tradePayload.getId());
        assertNull("not saved trade should return null", retrievedTrade);
    }

    @After
    public void cleanUp() {
        Session session = HibernateUtil.getInstance().getConnection();
        session.beginTransaction();
        session.createQuery("DELETE FROM TradePayload").executeUpdate();
        session.getTransaction().commit();
        session.close();
    }
}
