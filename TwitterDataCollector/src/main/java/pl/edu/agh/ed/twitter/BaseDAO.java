package pl.edu.agh.ed.twitter;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


@Transactional
public class BaseDAO {

    @Autowired
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    public SessionFactory sessionFactory() {
        return sessionFactory;
    }

    public Session session() {
        return sessionFactory.getCurrentSession();
    }
    
    public void save(Object object) {
        session().save(object);
    }

    public void delete(Object object) {
        session().delete(object);
    }

    public void update(Object object) {
        session().saveOrUpdate(object);
    }

}