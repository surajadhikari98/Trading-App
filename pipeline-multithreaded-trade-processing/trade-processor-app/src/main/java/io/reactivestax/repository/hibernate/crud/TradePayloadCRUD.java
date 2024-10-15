package io.reactivestax.repository.hibernate.crud;

import io.reactivestax.entity.TradePayload;
import io.reactivestax.utils.HibernateUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.List;

import static io.reactivestax.utils.Utility.checkValidity;


public class TradePayloadCRUD {

    public static synchronized void persistTradePayload(Session session, String payload) {
        if (payload != null) {
            String[] split = payload.split(",");
            TradePayload tradePayload = new TradePayload();
            tradePayload.setTradeId(split[0]);
            tradePayload.setValidityStatus(checkValidity(split) ? "valid" : "inValid");
            tradePayload.setStatusReason(checkValidity(split) ? "All field present " : "Fields missing");
            tradePayload.setLookupStatus("fail");
            tradePayload.setJeStatus("not_posted");
            tradePayload.setPayload(payload);

            Transaction transaction = session.beginTransaction();
            session.persist(tradePayload);
            transaction.commit();
        }
    }

    //using the criteria api for returning the count
    public static int readTradePayloadCount() {
        try (Session session = HibernateUtil.getInstance().getSession()) {
            final CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
            Root<TradePayload> root = query.from(TradePayload.class);
            query.select(criteriaBuilder.count(root));
            List<Long> resultList = session.createQuery(query).getResultList();
            return resultList.size();
        }
    }

    public static void updateLookUpAndJournalStatus(String tradeId) {
        try (Session session = HibernateUtil.getInstance().getSession()) {
            session.beginTransaction();
            TradePayload tradePayload = session.get(TradePayload.class, tradeId);
            tradePayload.setLookupStatus("pass");
            tradePayload.setJeStatus("posted");
            session.getTransaction().commit();
        }
    }

    //using the criteria api for returning the payloadByTradeId
    public static synchronized String readTradePayloadByTradeId(String tradeId) {
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