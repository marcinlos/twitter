package pl.edu.agh.ed.twitter;

import java.util.Collections;
import java.util.List;

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

public abstract class AbstractTweetFetcher extends AbstractProcessor<User> {
    
    @Autowired
    protected TwitterProvider provider;
    
    protected TwitterManager twitters;
    
    protected Twitter twitter;
    
    @PostConstruct
    public void initialize() {
        twitters = new TwitterManager(provider.getTwitters());
        twitter = twitters.getFor(CALL);
    }

    @Override
    protected Criterion fetchFilter() {
        Criterion ff = Restrictions.like("flag", flagPattern());               
        Criterion notDone  = Restrictions.eq("gotTweets", false);
        return Restrictions.and(ff, notDone);
    }
    
    protected abstract String flagPattern();
    
    protected abstract void processTweet(Tweet tweet, User user);
    
    protected abstract void processUser(User user);

    private static final String CALL = "/statuses/user_timeline";
    
    @Override
    protected int chunkSize() {
        return 1000;
    }
    
    private List<Status> getUserStatuses(long userId) {
        Sleeper netSleeper = new Sleeper(10, 1);
        Sleeper unknownSleeper = new Sleeper(5, 1);
        int unknownCount = 0;
        int maxUnknownRetries = 3;
        
        while (true) {
            try {
                return twitter.getUserTimeline(userId);
            } catch (TwitterException e) {
                if (e.exceededRateLimitation()) {
                    logger.error("Exceeded rate limitation");
                    twitter = twitters.getFor(CALL);
                } else if (e.isCausedByNetworkIssue()) {
                    logger.error("Network issues:", e);
                    netSleeper.sleep();
                } else if (e.resourceNotFound()) {
                    logger.warn("Resource not found");
                    return Collections.emptyList();
                } else {
                    if (e.getStatusCode() == 401) {
                        logger.error(
                                "Error 401, most likely uid={} does not exist",
                                userId);
                        throw new RuntimeException(e);
                    } else {
                        logger.error("Unknown error while fetching user", e);
                    }
                    ++ unknownCount;
                    logger.error("Unknown error {}/{}", unknownCount, 
                            maxUnknownRetries);
                    if (unknownCount >= maxUnknownRetries) {
                        logger.error("Max of {} retries in face of unknown "
                                + "error reached, skipping user {}", 
                                maxUnknownRetries, userId);
                        throw new RuntimeException(e);
                    }
                    logger.error("Waiting {} s ...", unknownSleeper.getDelay());
                    unknownSleeper.sleep();
                }
            }
        }
    }

    @Override
    protected void process(User user) {
        logger.info("User: @{}", user.getScreenName());
        List<Status> stats = getUserStatuses(user.getId());
        processStatusList(user, stats);
    }
    
    private void processStatusList(User user, List<Status> stats) {
        openSession();
        Transaction tx = session.beginTransaction();
        
        // Hibernate doesn't like it if we use detached user object as an
        // entity in Tweets objects we'll be likely persisting here
        user = users.get(user.getId());
        
        for (Status status : stats) {
            long id = status.getId();
            Tweet tweet = tweets.get(id);
            
            if (tweet == null) {
                tweet = Tweet.fromStatus(status, user);
            }
            
            processTweet(tweet, user);
            tweets.update(tweet);
        }
        processUser(user);
        users.update(user);
        
        tx.commit();
        closeSession();
    }

    @Override
    protected List<User> fetch(int first, int size, Criterion filter) {
        return users.getList(first, size, filter);
    }

}
