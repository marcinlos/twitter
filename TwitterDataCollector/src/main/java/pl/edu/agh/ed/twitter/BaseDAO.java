package pl.edu.agh.ed.twitter;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;


//@Transactional
public abstract class BaseDAO<Id extends Serializable, Entity> {

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
    
    public Entity get(Id id) {
        return (Entity) session().get(getEntityClass(), id);
    }
    
    public void save(Entity entity) {
        session().save(entity);
    }

    public void delete(Entity entity) {
        session().delete(entity);
    }
    
    public void deleteById(Id id) {
        delete(get(id));
    }
    
    public void update(Entity entity) {
        session().saveOrUpdate(entity);
    }
    
    protected abstract Class<Entity> getEntityClass();

}