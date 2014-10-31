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

import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import pl.edu.agh.ed.twitter.domain.Recommendation;
import pl.edu.agh.ed.twitter.domain.Tweet;
import pl.edu.agh.ed.twitter.domain.User;
import pl.edu.agh.ed.twitter.util.Sleeper;
import twitter4j.ResponseList;
import twitter4j.TwitterException;


@Component
public class FetchRecommendedImpl extends AbstractProcessor<Tweet> implements FetchRecommended {
    
    private static final int PER_REQ = 100;
    
    private final Set<String> fetchQueue = new HashSet<>(PER_REQ);
    private final Map<Long, Set<String>> cache = new HashMap<Long, Set<String>>(PER_REQ);
    
    private final static Pattern pat = Pattern.compile("@\\w+");
    
    @Override
    protected Criterion fetchFilter() {
        Criterion ff = Restrictions.like("flag", "*%");               
        Criterion notDone  = Restrictions.eq("gotRecommended", false);
        return Restrictions.and(ff, notDone);
    }

    @Override
    protected int chunkSize() {
        return 1000;
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

    @Override
    protected void process(Tweet tweet) {
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

    @Override
    protected List<Tweet> fetch(int first, int size, Criterion filter) {
        return tweets.getList(first, size, filter);
    }
    
}
