package io.reactivestax.repository.hibernate.crud;

import io.reactivestax.domain.Trade;
import io.reactivestax.entity.Position;
import io.reactivestax.utils.HibernateUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;

import java.math.BigInteger;

public class TradePositionCRUD {

    public static void persistPosition(Trade trade) {
        try (Session session = HibernateUtil.getInstance().getSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                Position position = new Position();
                position.setAccountNumber(trade.getAccountNumber());
                position.setCusip(trade.getCusip());
                position.setPosition(BigInteger.valueOf(trade.getQuantity()));
                session.persist(position);

                transaction.commit();

            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public static void updatePosition(Trade trade, int version) {
        try (Session session = HibernateUtil.getInstance().getSession()) {
            Transaction transaction = null;
            try {

                HibernateCriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaQuery<Position> query = cb.createQuery(Position.class);
                Root<Position> root = query.from(Position.class);

                Predicate accountNumberClause = cb.equal(root.get("accountNumber"), trade.getAccountNumber());
                Predicate cusipClause = cb.equal(root.get("cusip"), trade.getCusip());
                Predicate versionClause = cb.equal(root.get("version"), version);

                query.select(root).where(cb.and(accountNumberClause, cusipClause, versionClause)); //for OR we can use cb.or clause

                Position position = session.createQuery(query).uniqueResult();


                transaction = session.beginTransaction();
                if (position.getDirection().equalsIgnoreCase("BUY")) {
                    position.getPosition().add(BigInteger.valueOf(trade.getPosition()));
                } else {
                    position.getPosition().subtract(BigInteger.valueOf(trade.getPosition()));
                }

                position.setVersion(version + 1);

                session.persist(position);

                transaction.commit();

            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                    System.out.println(e.getMessage());
                }
            }
        }
    }


    //using the criteria api for returning the payloadByTradeId
    public static synchronized Integer getCusipVersion(Trade trade) {
        try (Session session = HibernateUtil.getInstance().getSession()) {
            final CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Integer> query = criteriaBuilder.createQuery(Integer.class);
            Root<Position> root = query.from(Position.class);
            query.select(root.get("version"));


            // Add multiple where conditions using criteriaBuilder.and()
            Predicate accountNumberPredicate = criteriaBuilder.equal(root.get("accountNumber"), trade.getAccountNumber());
            Predicate cusipPredicate = criteriaBuilder.equal(root.get("cusip"), trade.getCusip());

            // Combine predicates with AND
            query.where(criteriaBuilder.and(accountNumberPredicate, cusipPredicate));

            return session.createQuery(query).uniqueResult();
        }
    }


}
