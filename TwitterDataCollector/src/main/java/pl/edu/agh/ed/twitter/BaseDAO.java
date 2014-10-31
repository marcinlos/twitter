package pl.edu.agh.ed.twitter;

import java.io.Serializable;
import java.util.List;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
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
    
    public StatelessSession openStatelessSesion() {
        return sessionFactory.openStatelessSession();
    }

    public Session session() {
        return sessionFactory.getCurrentSession();
    }
    
    public long count(Criterion... predicates) {
        Number n = (Number) session()
                .createCriteria(getEntityClass())
                .add(Restrictions.and(predicates))
                .setProjection(Projections.rowCount())
                .uniqueResult();
        return n.longValue();
    }
    
    @SuppressWarnings("unchecked")
    public List<Entity> getList(Criterion... predicates) {
        return (List<Entity>) session()
                .createCriteria(getEntityClass())
                .add(Restrictions.and(predicates))
                .list();
    }
    
    @SuppressWarnings("unchecked")
    public List<Entity> getList(int first, int max, Criterion... predicates) {
        return (List<Entity>) session()
                .createCriteria(getEntityClass())
                .add(Restrictions.and(predicates))
                .setFirstResult(first)
                .setMaxResults(max)
                .list();
    }
    
    public ScrollableResults getCursor(ScrollMode mode, Criterion... predicates) {
        return session()
                .createCriteria(getEntityClass())
                .add(Restrictions.and(predicates))
                .scroll(mode);
    }
    
    @SuppressWarnings("unchecked")
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