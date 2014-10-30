package pl.edu.agh.ed.twitter;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import pl.edu.agh.ed.twitter.domain.User;

@Repository
public class UserDAO extends BaseDAO {

    public User getUser(long id) {
        Query query = session().createQuery("from User where id= :id");
        query.setParameter("id", id);
        List<?> list = query.list();
        if (list.isEmpty()) {
            return null;
        }
        return (User) list.get(0);
    }

}
