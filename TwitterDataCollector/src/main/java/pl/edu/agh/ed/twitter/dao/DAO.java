package pl.edu.agh.ed.twitter.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Criterion;

public interface DAO<Id extends Serializable, Entity> {
    
    long count(Criterion... predicates);
    
    List<Entity> getList(Criterion... predicates);
    
    List<Entity> getList(int first, int max, Criterion... predicates);
    
    ScrollableResults getCursor(ScrollMode mode, Criterion... predicates);
    
    Entity get(Id id);
    
    void save(Entity entity);
    
    void merge(Entity entity);

    void delete(Entity entity);
    
    void deleteById(Id id);
    
    void update(Entity entity);

}
