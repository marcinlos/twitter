package pl.edu.agh.ed.twitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.agh.ed.twitter.dao.DAO;
import pl.edu.agh.ed.twitter.dao.RecommendationDAO;
import pl.edu.agh.ed.twitter.dao.TweetDAO;
import pl.edu.agh.ed.twitter.dao.UserDAO;
import pl.edu.agh.ed.twitter.domain.Recommendation;
import pl.edu.agh.ed.twitter.domain.RecommendationID;
import pl.edu.agh.ed.twitter.domain.Tweet;
import pl.edu.agh.ed.twitter.domain.User;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;


@Component
public class FetchRecommendedImpl implements FetchRecommended {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TweetDAO mainTweetsDAO;
    private DAO<Long, Tweet> tweets;
    
    @Autowired
    private UserDAO mainUsersDAO;
    private DAO<Long, User> users;
    
    @Autowired
    private RecommendationDAO mainRecommendationsDAO;
    private DAO<RecommendationID, Recommendation> recommendations;
    
    @Autowired
    private Twitter twitter;
    
    @Autowired
    private SessionFactory sessionFactory;
    
    private static final int PER_REQ = 100;
    
    private final Set<String> fetchQueue = new HashSet<>(PER_REQ);
    private final Map<Long, Set<String>> cache = new HashMap<Long, Set<String>>(PER_REQ);

    
    private Session session;
    
    private final static Pattern pat = Pattern.compile("@\\w+");
    
    private static final Criterion FF = Restrictions.like("flag", "*%");
    private static final Criterion NOT_DONE = Restrictions.eq("gotRecommended", false);
    private static final Criterion TO_FETCH = Restrictions.and(FF, NOT_DONE);

    private static final int CHUNK_SIZE = 1000;
    
    private int first = 0;
    
    private void beginSession() {
        session = sessionFactory.openSession();
        tweets = mainTweetsDAO.with(session);
        users = mainUsersDAO.with(session);
        recommendations = mainRecommendationsDAO.with(session);
    }
    
    private void closeSession() {
        session.close();
        tweets = null;
        users = null;
        recommendations = null;
    }
    
    private static List<String> extractRecommended(String text) {
        List<String> recommended = new ArrayList<>();
        Matcher m = pat.matcher(text);
        while (m.find()) {
            // Omit '@'
            recommended.add(m.group().substring(1));
        }
        return recommended;
    }
    
    @Override
    public void run() {
        
        List<Tweet> chunk = nextChunk();
        while (! chunk.isEmpty()) {
            logger.info("Chunk {}-{}", first - chunk.size(), first - 1);
            
            for (Tweet tweet : chunk) {
                try {
                    process(tweet);
                } catch (Exception e) {
                    logger.error("During FF tweet processing", e);
                }
            }
            chunk = nextChunk();
        }
    }
    
    private List<User> getUsers(Collection<String> names) {
        Sleeper limitSleeper = new Sleeper(60, 2);
        Sleeper netSleeper = new Sleeper(30, 1);
        Sleeper unknownSleeper = new Sleeper(10, 1);
        
        if (names.isEmpty()) {
            return Collections.emptyList();
        }
        
        while (true) {
            try {
                String[] array = names.toArray(new String[0]);
                ResponseList<twitter4j.User> response = twitter.lookupUsers(array);
                List<User> users = new ArrayList<>();
                for (twitter4j.User u : response) {
                    users.add(User.fromUser(u));
                }
                return users;
            } catch (TwitterException e) {
                if (e.exceededRateLimitation()) {
                    logger.error("Exceeded rate limitation");
                    limitSleeper.sleep();
                } else if (e.isCausedByNetworkIssue()) {
                    logger.error("Network issues: {}", e);
                    netSleeper.sleep();
                } else if (e.resourceNotFound()) {
                    logger.warn("Resource not found");
                    return Collections.emptyList();
                } else {
                    logger.error("Unknown error while fetching user", e);
                    unknownSleeper.sleep();
                }
            }
        }
    }
    
    private List<User> userByName(String name) {
        return users.getList(Restrictions.eq("screenName", name));
    }
    
    
    private void saveRecommendations(Tweet tweet, Iterable<User> all) {
        Transaction tx = session.beginTransaction();
        for (User user : all) {
            user.addFlag(User.RECOMMENDED);
            Recommendation rec = new Recommendation(tweet, user);
            users.update(user);
            recommendations.merge(rec);
        }
        tx.commit();
    }

    private void process(Tweet tweet) {
        beginSession();
        
        String text = tweet.getText();
        Set<String> recommended = new HashSet<>(extractRecommended(text));
        
        Set<String> toFetch = new HashSet<>();
        Set<User> all = new HashSet<>();
        
        for (String name : recommended) {
            
            List<User> us = userByName(name);
            
            if (us.isEmpty()) {
                toFetch.add(name);
            } else {
                User user = us.get(0);
                all.add(user);
            }
        }
        
        saveRecommendations(tweet, all);
        if (! toFetch.isEmpty()) {
            queue(toFetch);
            cache.put(tweet.getId(), toFetch);
        } else {
            tweet.setGotRecommended(true);
            
            Transaction tx = session.beginTransaction();
            tweets.update(tweet);
            tx.commit();
        }
        
        closeSession();
    }
    
    private Map<String, User> byNames(Collection<User> users) {
        Map<String, User> data = new HashMap<>(users.size());
        
        for (User user : users) {
            String name = user.getScreenName().toLowerCase();
            // logger.info("{} -> {}", name, user.getId());
            data.put(name, user);
        }
        
        return data;
    }
    
    private void queue(Collection<String> names) {
        if (names.size() + fetchQueue.size() > PER_REQ) {
            logger.info("Clearing fetch queue...");
            clearFetchQueue();
        }
        fetchQueue.addAll(names);
        logger.info("Fetch queue: {}/{}", fetchQueue.size(), PER_REQ);
    }
    
    
    private void clearFetchQueue() {
        int total = fetchQueue.size();
        Map<String, User> users = byNames(getUsers(fetchQueue));
        fetchQueue.clear();
        
        logger.info("Managed to fetch {} of {} users", users.size(), total);
        
        int missing = 0;
        for (Entry<Long, Set<String>> pair : cache.entrySet()) {
            long id = pair.getKey();
            Set<String> recommended = pair.getValue();
            
            List<User> toAdd = new ArrayList<>();

            // logger.info("Tweet [id={}]:", id);
            for (String name : recommended) {
                User user = users.get(name.toLowerCase());
                
                if (user == null) {
                    logger.info("[id={}] Missing user: {}", id, name);
                    ++ missing;
                } else {
                    toAdd.add(user);
                }
            }
            Tweet tweet = tweets.get(id);
            saveRecommendations(tweet, toAdd);
            tweet.setGotRecommended(true);
            
            Transaction tx = session.beginTransaction();
            tweets.update(tweet);
            tx.commit();
        }
        logger.info("{} tweets, {} missing recommended users", cache.size(), missing);
        cache.clear();
    }
    
    public List<Tweet> nextChunk() {
        beginSession();
        List<Tweet> list = tweets.getList(first, CHUNK_SIZE, TO_FETCH);
        first += list.size();
        closeSession();
        return list;
    }

}
