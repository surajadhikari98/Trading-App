package io.reactivestax.repository.hibernate;

import io.reactivestax.contract.repository.PositionRepository;
import io.reactivestax.model.Trade;
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
import java.sql.SQLException;

public class HibernateTradePositionRepository implements PositionRepository {

    private static HibernateTradePositionRepository instance;

    private HibernateTradePositionRepository() {
    }

    public static synchronized HibernateTradePositionRepository getInstance() {
        if (instance == null) {
            instance = new HibernateTradePositionRepository();
        }
        return instance;
    }

    @Override
    public boolean insertPosition(Trade trade) throws SQLException {
        Session session = HibernateUtil.getInstance().getConnection();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Position position = new Position();
            position.setAccountNumber(trade.getAccountNumber());
            position.setCusip(trade.getCusip());
            position.setPosition(BigInteger.valueOf(trade.getQuantity()));
            position.setDirection(trade.getDirection());
            session.persist(position);
            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
                return false;
            }
        }
        return true;
    }

    public boolean updatePosition(Trade trade, int version) throws Exception {
        Session session = HibernateUtil.getInstance().getConnection();
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
                return false;
            }

        }
        return true;
    }


    //using the criteria api for returning the payloadByTradeId
    public Integer getCusipVersion(Trade trade) {
        Session session = HibernateUtil.getInstance().getConnection();
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

