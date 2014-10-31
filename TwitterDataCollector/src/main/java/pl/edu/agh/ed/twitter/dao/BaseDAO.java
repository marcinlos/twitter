package pl.edu.agh.ed.twitter.dao;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseDAO<Id extends Serializable, Entity> extends
        AbstractDAO<Id, Entity> {

    @Autowired
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public SessionFactory sessionFactory() {
        return sessionFactory;
    }

    public StatelessSession openStatelessSesion() {
        return sessionFactory.openStatelessSession();
    }

    public Session session() {
        return sessionFactory.getCurrentSession();
    }
    
    public DAO<Id, Entity> with(final Session session) {
        return new AbstractDAO<Id, Entity>() {

            @Override
            public Session session() {
                return session;
            }

            @Override
            protected Class<Entity> getEntityClass() {
                return BaseDAO.this.getEntityClass();
            }
        };
    }

}