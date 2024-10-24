package io.reactivestax.repository.hibernate;

import io.reactivestax.contract.repository.PayloadRepository;
import io.reactivestax.model.Trade;
import io.reactivestax.entity.TradePayload;
import io.reactivestax.enums.LookUpStatusEnum;
import io.reactivestax.enums.PostedStatusEnum;
import io.reactivestax.enums.ValidityStatusEnum;
import io.reactivestax.utils.HibernateUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;

import java.util.List;

import static io.reactivestax.utils.Utility.prepareTrade;


public class HibernateTradePayloadRepository implements PayloadRepository {

    private static HibernateTradePayloadRepository instance;

    private HibernateTradePayloadRepository() {
    }

    public static synchronized HibernateTradePayloadRepository getInstance() {
        if (instance == null) {
            instance = new HibernateTradePayloadRepository();
        }
        return instance;
    }

    @Override
    public void insertTradeIntoTradePayloadTable(String payload) throws Exception {
        Session session = HibernateUtil.getInstance().getConnection();
        Trade trade = prepareTrade(payload);
        TradePayload tradePayload = new TradePayload();
        tradePayload.setTradeId(trade.getTradeIdentifier());
        tradePayload.setValidityStatus(payload != null ? String.valueOf(ValidityStatusEnum.VALID) : String.valueOf(ValidityStatusEnum.INVALID));
        tradePayload.setStatusReason(payload != null ? "All field present " : "Fields missing");
        tradePayload.setLookupStatus(String.valueOf(LookUpStatusEnum.FAIL));
        tradePayload.setJeStatus(String.valueOf(PostedStatusEnum.NOT_POSTED));
        tradePayload.setPayload(payload);
        session.persist(tradePayload);
    }

    //using the criteria api for returning the count
    public int readTradePayloadCount() {
        Session session = HibernateUtil.getInstance().getConnection();
        final CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<TradePayload> root = query.from(TradePayload.class);
        query.select(criteriaBuilder.count(root));
        List<Long> resultList = session.createQuery(query).getResultList();
        return resultList.size();

    }

    @Override
    public void updateLookUpStatus(String tradeId) {
        Session session = HibernateUtil.getInstance().getConnection();
        session.beginTransaction();
        TradePayload tradePayload = session.createQuery("FROM TradePayload WHERE tradeId = :tradeId", TradePayload.class)
                .setParameter("tradeId", tradeId)
                .uniqueResult();

        tradePayload.setLookupStatus(String.valueOf(LookUpStatusEnum.PASS));
        session.persist(tradePayload);
        session.getTransaction().commit();

    }


    @Override
    public void updateJournalStatus(String tradeId) {
        Session session = HibernateUtil.getInstance().getConnection();
        session.beginTransaction();
        TradePayload tradePayload = session.createQuery("FROM TradePayload WHERE tradeId = :tradeId", TradePayload.class)
                .setParameter("tradeId", tradeId)
                .uniqueResult();
        tradePayload.setJeStatus(String.valueOf(PostedStatusEnum.POSTED));
        session.persist(tradePayload);
        session.getTransaction().commit();
    }


    //using the criteria api for returning the payloadByTradeId
    @Override
    public String readTradePayloadByTradeId(String tradeId) {
        Session session = HibernateUtil.getInstance().getConnection();
        final CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<String> query = criteriaBuilder.createQuery(String.class);
        Root<TradePayload> root = query.from(TradePayload.class);
        query.select(root.get("payload"));
        query.where(criteriaBuilder.equal(root.get("tradeId"), tradeId));

        // Limit the result to only 1 record
        return session.createQuery(query)
                .setMaxResults(1)
                .getSingleResult();
    }


}