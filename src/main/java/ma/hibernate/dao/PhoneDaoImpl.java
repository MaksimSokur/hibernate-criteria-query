package ma.hibernate.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = factory.openSession();
            transaction = session.beginTransaction();
            session.persist(phone);
            transaction.commit();
            return phone;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Can't create phone " + phone, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        try (Session session = factory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Phone> query = criteriaBuilder.createQuery(Phone.class);
            Root<Phone> phoneRoot = query.from(Phone.class);

            Predicate globalPredicate = null;
            for (String key : params.keySet()) {
                CriteriaBuilder.In<String> fieldCb =
                        criteriaBuilder.in(phoneRoot.get(key));
                for (String fieldValue : params.keySet()) {
                    fieldCb.value(fieldValue);
                }
                globalPredicate = globalPredicate == null ? fieldCb :
                        criteriaBuilder.and(globalPredicate, fieldCb);
            }
            return globalPredicate == null
                   ? session.createQuery("from Phone", Phone.class).getResultList()
                   : session.createQuery(query.where(globalPredicate)).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Can't find all phones with params"
                       + params + ". Error", e);
        }
    }
}
