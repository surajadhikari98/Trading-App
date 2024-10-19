package io.reactivestax.repository.hibernate;

import io.reactivestax.contract.repository.PayloadRepository;
import io.reactivestax.domain.Trade;
import io.reactivestax.entity.TradePayload;
import io.reactivestax.enums.LookUpStatusEnum;
import io.reactivestax.enums.PostedStatusEnum;
import io.reactivestax.enums.ValidityStatusEnum;
import io.reactivestax.utils.HibernateUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;


public class HibernateTradePayloadRepository implements PayloadRepository {

    private static HibernateTradePayloadRepository instance;

    private HibernateTradePayloadRepository() {}

    public static synchronized HibernateTradePayloadRepository getInstance() {
        if (instance == null) {
            instance = new HibernateTradePayloadRepository();
        }
        return instance;
    }

    @Override
    public void insertTradeIntoTradePayloadTable(Trade payload) throws Exception {
        try (Session session = HibernateUtil.getInstance().getSession()) {
//            String[] split = payload.split(",");
            TradePayload tradePayload = new TradePayload();
            tradePayload.setTradeId(payload.getTradeIdentifier());
            tradePayload.setValidityStatus(payload != null ? String.valueOf(ValidityStatusEnum.VALID) : String.valueOf(ValidityStatusEnum.INVALID));
            tradePayload.setStatusReason(payload != null ? "All field present " : "Fields missing");
            tradePayload.setLookupStatus(String.valueOf(LookUpStatusEnum.FAIL));
            tradePayload.setJeStatus(String.valueOf(PostedStatusEnum.NOT_POSTED));
            tradePayload.setPayload(String.valueOf(payload));
            Transaction transaction = session.beginTransaction();
            session.persist(tradePayload);
            transaction.commit();
        }
    }

    //using the criteria api for returning the count
    public int readTradePayloadCount() {
        try (Session session = HibernateUtil.getInstance().getSession()) {
            final CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
            Root<TradePayload> root = query.from(TradePayload.class);
            query.select(criteriaBuilder.count(root));
            List<Long> resultList = session.createQuery(query).getResultList();
            return resultList.size();
        }
    }

    @Override
    public void updateLookUpStatus(String tradeId) throws SQLException, FileNotFoundException {
        try (Session session = HibernateUtil.getInstance().getSession()) {
            session.beginTransaction();
            TradePayload tradePayload = session.get(TradePayload.class, tradeId);
            tradePayload.setLookupStatus(String.valueOf(LookUpStatusEnum.PASS));
            session.getTransaction().commit();
        }
    }


    @Override
    public void updateJournalStatus(String tradeId) throws SQLException, FileNotFoundException {
        try (Session session = HibernateUtil.getInstance().getSession()) {
            session.beginTransaction();
            TradePayload tradePayload = session.get(TradePayload.class, tradeId);
            tradePayload.setJeStatus(String.valueOf(PostedStatusEnum.POSTED));
            session.getTransaction().commit();
        }
    }

    //using the criteria api for returning the payloadByTradeId
    @Override
    public String readTradePayloadByTradeId(String tradeId) {
        try (Session session = HibernateUtil.getInstance().getSession()) {
            final CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<String> query = criteriaBuilder.createQuery(String.class);
            Root<TradePayload> root = query.from(TradePayload.class);
            query.select(root.get("payload"));
            query.where(criteriaBuilder.equal(root.get("tradeId"), tradeId)); //the field has to match the entity name not the db name
            return session.createQuery(query).getSingleResult();
        }
    }



}