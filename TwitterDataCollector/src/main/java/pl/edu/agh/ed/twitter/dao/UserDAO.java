package pl.edu.agh.ed.twitter.dao;

import org.springframework.stereotype.Repository;

import pl.edu.agh.ed.twitter.domain.User;

@Repository
public class UserDAO extends BaseDAO<Long, User> {

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

}
