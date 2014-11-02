package pl.edu.agh.ed.twitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;

import pl.edu.agh.ed.twitter.domain.Tweet;
import pl.edu.agh.ed.twitter.domain.User;
import pl.edu.agh.ed.twitter.util.Sleeper;
import pl.edu.agh.ed.twitter.util.TwitterManager;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;


public abstract class AbstractRetweetsFetcher extends AbstractProcessor<Tweet> {

    @Autowired
    protected TwitterProvider provider;

    protected TwitterManager twitters;

    protected Twitter twitterForUsers;
    protected Twitter twitterForRetweets;


    private static final int QUEUE_SIZE = 100;
    private final List<Tweet> queue = new ArrayList<>(QUEUE_SIZE);

    private static final String GET_USERS = "/users/lookup";
    private static final String GET_RETWEETS = "/statuses/retweets/:id";

    @PostConstruct
    public void initialize() {
        twitters = new TwitterManager(provider.getTwitters());
        obtainTwitters();
    }

    private void obtainTwitters() {
        twitterForUsers = twitters.getFor(GET_USERS);
        twitterForRetweets = twitters.getFor(GET_RETWEETS);
    }

    @Override
    protected Criterion fetchFilter() {
        Criterion ff = Restrictions.like("flag", flagPattern());               
        Criterion notDone  = Restrictions.eq("gotRetweets", false);
        Criterion notProblem = Restrictions.ne("cantGetRetweets", true);
        Criterion notRetweet = Restrictions.eq("retweetedStatus", 0L);
        return Restrictions.and(ff, notDone, notProblem, notRetweet);
    }

    protected abstract String flagPattern();

    protected abstract void postProcessOriginalTweet(Tweet tweet);
    protected abstract void postProcessRetweet(Tweet retweet, Tweet orig);


    @Override
    protected int chunkSize() {
        return 1000;
    }

    private long[] getIdArray(Collection<Tweet> tweets) {
        long[] ids = new long[tweets.size()];
        int i = 0;
        for (Tweet tweet : tweets) {
            ids[i++] = tweet.getId();
        }
        return ids;
    }
    
    
    private List<Tweet> getUpToDateTweets(Collection<Tweet> old) {
        long[] ids = getIdArray(old);
        
        Sleeper netSleeper = new Sleeper(30, 1);
        Sleeper unknownSleeper = new Sleeper(10, 1);
        int unknownCount = 0;
        int maxUnknownRetries = 3;
        
        while (true) {
            try {
                List<Status> response = twitterForUsers.lookup(ids);
                List<Tweet> tweets = new ArrayList<>();
                for (Status tweet : response) {
                    User user = User.fromUser(tweet.getUser());
                    tweets.add(Tweet.fromStatus(tweet, user));
                }
                return tweets;
            } catch (TwitterException e) {
                if (e.exceededRateLimitation()) {
                    logger.error("Exceeded rate limitation");
                    obtainTwitters();
                } else if (e.isCausedByNetworkIssue()) {
                    logger.error("Network issues: {}", e);
                    netSleeper.sleep();
                } else if (e.resourceNotFound()) {
                    logger.warn("Resource not found");
                    return Collections.emptyList();
                } else {
                    ++ unknownCount;
                    logger.error("Unknown error {}/{}", unknownCount, 
                            maxUnknownRetries);
                    if (unknownCount >= maxUnknownRetries) {
                        logger.error("Max of {} retries in face of unknown "
                                + "error reached, skipping this part for now", 
                                maxUnknownRetries);
                        throw new RuntimeException(e);
                    }
                    logger.error("Waiting {} s ...", unknownSleeper.getDelay());
                    unknownSleeper.sleep();
                }
            }
        }
    }

    @Override
    protected void afterJob() {
        clearQueue();
    }
    
    private List<Status> getReweets(Tweet tweet) {
        Sleeper netSleeper = new Sleeper(30, 1);
        Sleeper unknownSleeper = new Sleeper(10, 1);
        int unknownCount = 0;
        int maxUnknownRetries = 3;
        
        long id = tweet.getId();
        
        while (true) {
            try {
                return twitterForRetweets.getRetweets(id);
            } catch (TwitterException e) {
                if (e.exceededRateLimitation()) {
                    logger.error("Exceeded rate limitation");
                    obtainTwitters();
                } else if (e.isCausedByNetworkIssue()) {
                    logger.error("Network issues: {}", e);
                    netSleeper.sleep();
                } else if (e.resourceNotFound()) {
                    logger.warn("Resource not found");
                    return Collections.emptyList();
                } else {
                    ++ unknownCount;
                    logger.error("Unknown error {}/{}", unknownCount, 
                            maxUnknownRetries);
                    if (unknownCount >= maxUnknownRetries) {
                        logger.error("Max of {} retries in face of unknown "
                                + "error reached, skipping this part for now", 
                                maxUnknownRetries);
                        throw new RuntimeException(e);
                    }
                    logger.error("Waiting {} s ...", unknownSleeper.getDelay());
                    unknownSleeper.sleep();
                }
            }
        }
    }
    
    
    private void enqueue(Tweet tweet) {
        queue.add(tweet);
        
        if (queue.size() == QUEUE_SIZE) {
            clearQueue();
        }
    }

    private Set<Long> ids(Collection<Tweet> tweets) {
        Set<Long> ids = new HashSet<>();
        for (Tweet tweet : tweets) {
            ids.add(tweet.getId());
        }
        return ids;
    }
    
    private Set<Long> difference(Collection<Tweet> a, Collection<Tweet> b) {
        Set<Long> as = ids(a);
        Set<Long> bs = ids(b);
        as.removeAll(bs);
        return as;
    }
    
    private void handleMissing(Collection<Long> missing) {
        logger.warn("Missing tweets:");
        for (Long id : missing) {
            logger.warn("  - {}", id);
        }

        openSession();
        Transaction tx = beginTransaction();
        
        for (Long id : missing) {
            Tweet tweet = tweets.get(id);
            tweet.setCantGetRetweets(true);
            tweets.update(tweet);
        }
        
        tx.commit();
        closeSession();
    }
    
    private void clearQueue() {
        int total = queue.size();
        
        List<Tweet> fresh = getUpToDateTweets(queue);
        logger.info("Managed to fetch {} of {} tweets", fresh.size(), total);
        
        Set<Long> missing = difference(queue, fresh);
        if (! missing.isEmpty()) {
            handleMissing(missing);
        }
        
        queue.clear();
        
        int withRetweets = 0;
        for (Tweet tweet : fresh) {
            if (tweet.getRetweetCount() > 0) {
                ++ withRetweets;
                processSingleTweet(tweet);
            } else {
                processRetweetList(tweet, Collections.<Status>emptyList());
            }
        }
        
        logger.info("Done, {}/{} had retweets", withRetweets, fresh.size());
    }
    
    private void processSingleTweet(Tweet tweet) {
        List<Status> retweets = getReweets(tweet);
        processRetweetList(tweet, retweets);
    }

    @Override
    protected void process(Tweet tweet) {
//        logger.info("Tweet [id={}]", tweet.getId());
        enqueue(tweet);
    }
    
    private User getAuthor(Status status) {
        long id = status.getUser().getId();
        User user = users.get(id);
        
        if (user == null) {
            user = User.fromUser(status.getUser());
        }
        return user;
    }
    
    private void processRetweetList(Tweet tweet, List<Status> retweets) {
        openSession();
        Transaction tx = session.beginTransaction();
        
        // Hibernate doesn't like it if we use detached user object as an
        // entity in Tweets objects we'll be likely persisting here
        tweet = tweets.get(tweet.getId());
        
        for (Status status : retweets) {
            User user = getAuthor(status);
            Tweet retweet = Tweet.fromStatus(status, user);
            postProcessRetweet(retweet, tweet);
            
            users.update(user);
            tweets.update(retweet);
        }
        postProcessOriginalTweet(tweet);
        tweets.update(tweet);
        
        tx.commit();
        closeSession();
    }

    @Override
    protected List<Tweet> fetch(int first, int size, Criterion filter) {
        return tweets.getList(first, size, filter);
    }

}
