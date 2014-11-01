package pl.edu.agh.ed.twitter;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import pl.edu.agh.ed.twitter.dao.DAO;
import pl.edu.agh.ed.twitter.dao.RecommendationDAO;
import pl.edu.agh.ed.twitter.dao.TweetDAO;
import pl.edu.agh.ed.twitter.dao.UserDAO;
import pl.edu.agh.ed.twitter.domain.Recommendation;
import pl.edu.agh.ed.twitter.domain.RecommendationID;
import pl.edu.agh.ed.twitter.domain.Tweet;
import pl.edu.agh.ed.twitter.domain.User;

public class SessionManager {

    @Autowired
    private TweetDAO mainTweetsDAO;
    protected DAO<Long, Tweet> tweets;
    
    @Autowired
    private UserDAO mainUsersDAO;
    protected DAO<Long, User> users;
    
    @Autowired
    private RecommendationDAO mainRecommendationsDAO;
    protected DAO<RecommendationID, Recommendation> recommendations;
    
    @Autowired
    private SessionFactory sessionFactory;
    
    protected Session session;

    public void openSession() {
        session = sessionFactory.openSession();

        tweets = mainTweetsDAO.with(session);
        users = mainUsersDAO.with(session);
        recommendations = mainRecommendationsDAO.with(session);
    }

    public void closeSession() {
        session.close();

        tweets = null;
        users = null;
        recommendations = null;
    }
    
    public Session getCurrentSession() {
        return session;
    }
    
    public Transaction beginTransaction() {
        return session.beginTransaction();
    }

}