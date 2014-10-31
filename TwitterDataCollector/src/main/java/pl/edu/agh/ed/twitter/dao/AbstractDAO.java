package pl.edu.agh.ed.twitter.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public abstract class AbstractDAO<Id extends Serializable, Entity> implements
        DAO<Id, Entity> {

    public abstract Session session();

    protected abstract Class<Entity> getEntityClass();

    @Override
    public long count(Criterion... predicates) {
        Number n = (Number) session()
                .createCriteria(getEntityClass())
                .add(Restrictions.and(predicates))
                .setProjection(Projections.rowCount())
                .uniqueResult();
        return n.longValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Entity> getList(Criterion... predicates) {
        return (List<Entity>) session()
                .createCriteria(getEntityClass())
                .add(Restrictions.and(predicates))
                .list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Entity> getList(int first, int max, Criterion... predicates) {
        return (List<Entity>) session()
                .createCriteria(getEntityClass())
                .add(Restrictions.and(predicates))
                .setFirstResult(first)
                .setMaxResults(max)
                .list();
    }

    @Override
    public ScrollableResults getCursor(ScrollMode mode, Criterion... predicates) {
        return session()
                .createCriteria(getEntityClass())
                .add(Restrictions.and(predicates))
                .scroll(mode);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Entity get(Id id) {
        return (Entity) session().get(getEntityClass(), id);
    }
    
    @Override
    public void save(Entity entity) {
        session().save(entity);
    }
    
    @Override
    public void merge(Entity entity) {
        session().merge(entity);
    }

    @Override
    public void delete(Entity entity) {
        session().delete(entity);
    }

    @Override
    public void deleteById(Id id) {
        delete(get(id));
    }

    @Override
    public void update(Entity entity) {
        session().saveOrUpdate(entity);
    }

}
