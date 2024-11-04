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
    }


    @Test
    public void testSingleInstanceCreation(){
        HibernateUtil instance = HibernateUtil.getInstance();
        HibernateUtil instance1 = HibernateUtil.getInstance();
        assertEquals(instance.hashCode(), instance1.hashCode());
    }

    @Test
    public void testTransactionCommit() {
        HibernateUtil instance = HibernateUtil.getInstance();
        Session session = instance.getConnection();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            TradePayload tradePayload = new TradePayload();
            tradePayload.setTradeId("1");
            tradePayload.setValidityStatus(String.valueOf(ValidityStatusEnum.VALID));
            tradePayload.setStatusReason("All field present ");
            tradePayload.setLookupStatus(String.valueOf(LookUpStatusEnum.FAIL));
            tradePayload.setJeStatus(String.valueOf(PostedStatusEnum.NOT_POSTED));
            tradePayload.setPayload("");
            session.persist(tradePayload);
            transaction.commit();
            assertTrue(session.contains("Trade payload should be in session", tradePayload));
            TradePayload retrievedTradePayload = session.createQuery("FROM TradePayload WHERE tradeId = :tradeId", TradePayload.class)
                    .setParameter("tradeId", "1")
                    .uniqueResult();
            assertNotNull(retrievedTradePayload);
            assertEquals("trade payload and retrieved value should be equal", tradePayload, retrievedTradePayload);
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            cleanUp();
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


   @Test
    public void testCloseConnection(){
        Session session = HibernateUtil.getInstance().getConnection();
        assertNotNull(session);
        assertTrue("session should be open", session.isOpen());
        session.close();
        assertFalse("Session should be closed", session.isOpen());
        HibernateUtil.getThreadLocalSession().remove();
        Session currentSession = HibernateUtil.getThreadLocalSession().get();
        assertNull("Thread Local should no longer hold the session ", currentSession);
    }

    public void cleanUp() {
        Session session = HibernateUtil.getInstance().getConnection();
        session.beginTransaction();
        session.createQuery("DELETE FROM TradePayload").executeUpdate();
        session.getTransaction().commit();
    }
}
