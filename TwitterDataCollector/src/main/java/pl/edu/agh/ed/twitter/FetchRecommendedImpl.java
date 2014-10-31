package pl.edu.agh.ed.twitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.agh.ed.twitter.domain.Tweet;
import pl.edu.agh.ed.twitter.domain.User;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;


@Component
public class FetchRecommendedImpl implements FetchRecommended {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TweetDAO tweets;
    
    @Autowired
    private UserDAO users;
    
    @Autowired
    private Twitter twitter;
    
    @Autowired
    private SessionFactory sessionFactory;
    
    private Session session;
    
    private final static Pattern pat = Pattern.compile("@\\w+");
    
    private static final Criterion FF = Restrictions.like("flag", "*%");
    private static final Criterion NOT_DONE = Restrictions.eq("gotRetweets", false);
    private static final Criterion TO_FETCH = Restrictions.and(FF, NOT_DONE);

    private static final int CHUNK_SIZE = 1000;
    
    private int first = 0;
    
    private static List<String> extractRecommended(String text) {
        List<String> recommended = new ArrayList<>();
        Matcher m = pat.matcher(text);
        while (m.find()) {
            // Omit '@'
            recommended.add(m.group().substring(1));
        }
        return recommended;
    }
    
    private void test() throws TwitterException {
        
        
        String[] names = {"apaner5984an23nzm54", "null"};
        ResponseList<twitter4j.User> users = twitter.lookupUsers(names);
        for (twitter4j.User u : users) {
            if (u == null) {
                logger.info("Dupa, null");
            } else {
                User user = User.fromUser(u);
                logger.info(formatUser(user));
            }
        }
    }
    
    @Override
    public void run() {
//        
//        try {
//            test();
//        } catch (TwitterException e) {
//            e.printStackTrace();
//        }
        
        List<Tweet> chunk = nextChunk();
        while (! chunk.isEmpty()) {
            logger.info("Chunk {}-{}", first - chunk.size(), first - 1);
            session = sessionFactory.openSession();
            
            for (Tweet tweet : chunk) {
                try {
                    process(tweet);
                } catch (Exception e) {
                    logger.error("During FF tweet processing", e);
                }
            }
            session.close();
            chunk = nextChunk();
        }
    }
    
    private User getUser(String name) {
        Sleeper limitSleeper = new Sleeper(60, 2);
        Sleeper netSleeper = new Sleeper(30, 1);
        
        while (true) {
            try {
                twitter4j.User user = twitter.showUser(name);
                return User.fromUser(user);
            } catch (TwitterException e) {
                if (e.resourceNotFound()) {
                    logger.error("User not found: {}", name);
                    return null;
                } else if (e.exceededRateLimitation()) {
                    logger.error("Exceeded rate limitation");
                    limitSleeper.sleep();
                } else if (e.isCausedByNetworkIssue()) {
                    logger.error("Network issues: {}", e);
                    netSleeper.sleep();
                } else {
                    logger.error("Unknown error while fetching user", e);
                }
            }
        }
    }
    
    
    private List<User> getUsers(List<String> names) {
        Sleeper limitSleeper = new Sleeper(60, 2);
        Sleeper netSleeper = new Sleeper(30, 1);
        
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
                } else {
                    logger.error("Unknown error while fetching user", e);
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    private String formatUser(User user) {
        StringBuilder sb = new StringBuilder("User {\n");
        sb.append("   id         = " + user.getId() + ",\n");
        sb.append("   name       = " + user.getName() + ",\n");
        sb.append("   screenName = " + user.getScreenName() + ",\n");
        sb.append("   followers  = " + user.getFollowers() + ",\n");
        sb.append("   follows    = " + user.getFollowings() + ",\n");
        sb.append("   language   = " + user.getLanguage() + ",\n");
        sb.append("   favourites = " + user.getFavourites() + ",\n");
        sb.append("   tweets     = " + user.getStatuses() + ",\n");
        sb.append("}");
        return sb.toString();
    }
    
    private void outputUser(User user) {
        logger.info(":\n" + formatUser(user));
    }
    
    private List<User> userByName(String name) {
        return users.with(session).getList(Restrictions.eq("screenName", name));
    }

    private void process(Tweet tweet) {
        String text = tweet.getText();
        List<String> recommended = extractRecommended(text);
        logger.info("Tweet [id={}], {} recommended:", tweet.getId(), recommended.size());
        
        List<String> toFetch = new ArrayList<>();
        List<User> all = new ArrayList<>();
        for (String name : recommended) {
            
            List<User> us = userByName(name);
            
            if (us.isEmpty()) {
                toFetch.add(name);
            } else {
                User user = us.get(0);
                all.add(user);
            }
        }
        logger.info("{} present, {} to fetch", all.size(), toFetch.size());
        
        List<User> fetched = getUsers(toFetch);
        
        for (User user : fetched) {
            toFetch.remove(user.getScreenName());
        }
        
        logger.info("{} fetched, {} missing", fetched.size(), toFetch.size());
        for (String name: toFetch) {
            logger.info(" - {}", name);
        }
        
        all.addAll(fetched);
        
        for (User user : all) {
            user.addFlag(User.RECOMMENDED);
//            users.with(session).update(user);
        }
        
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            
        }
    }
    
    public List<Tweet> nextChunk() {
        Session session = sessionFactory.openSession();
        
        List<Tweet> list = tweets.with(session).getList(first, CHUNK_SIZE, TO_FETCH);
        first += list.size();
        
        session.close();
        return list;
    }

}
