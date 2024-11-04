package io.reactivestax.repository.hibernate;

import io.reactivestax.types.contract.repository.SecuritiesReferenceRepository;
import io.reactivestax.repository.hibernate.entity.SecuritiesReference;
import io.reactivestax.utility.HibernateUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;

import java.util.List;

public class HibernateSecuritiesReferenceRepository implements SecuritiesReferenceRepository {
    private static HibernateSecuritiesReferenceRepository instance;

    private HibernateSecuritiesReferenceRepository() {
    }

    public static synchronized HibernateSecuritiesReferenceRepository getInstance() {
        if (instance == null) {
            instance = new HibernateSecuritiesReferenceRepository();
        }
        return instance;
    }

    @Override
    public boolean lookupSecurities(String cusip) {
       Session session = HibernateUtil.getInstance().getConnection();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        // Step 2: Create CriteriaQuery
        CriteriaQuery<SecuritiesReference> cq = cb.createQuery(SecuritiesReference.class);

        // Step 3: Define the Root (FROM clause)
        Root<SecuritiesReference> root = cq.from(SecuritiesReference.class);

        // Step 4: Define the WHERE clause
        cq.select(root).where(cb.equal(root.get("cusip"), cusip));

        // Step 5: Execute the query
        List<SecuritiesReference> cusipList = session.createQuery(cq).getResultList();

        // Return true if the list is not empty, meaning the cusip was found
        return !cusipList.isEmpty();
    }
}
