package pl.edu.agh.ed.twitter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import pl.edu.agh.ed.twitter.domain.Tweet;
import pl.edu.agh.ed.twitter.domain.User;
import pl.edu.agh.ed.twitter.util.Sleeper;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.TwitterException;

public abstract class BasicTweetFetcher extends AbstractProcessor<User> {

    @Override
    protected Criterion fetchFilter() {
        Criterion ff = Restrictions.like("flag", flagPattern());               
        Criterion notDone  = Restrictions.eq("gotTweets", false);
        return Restrictions.and(ff, notDone);
    }
    
    protected abstract String flagPattern();
    
    protected abstract void processTweet(Tweet tweet, User user);
    
    protected abstract void processUser(User user);

    
    @Override
    protected int chunkSize() {
        return 1000;
    }
    
    private int findResetDelay(String name) {
        Sleeper netSleeper = new Sleeper(30, 1);
        while (true) {
            try {
                Map<String, RateLimitStatus> limits = twitter
                        .getRateLimitStatus();
                RateLimitStatus status = limits.get(name);
                return status.getSecondsUntilReset();
            } catch (TwitterException e) {
                if (e.exceededRateLimitation()) {
                    logger.error("Exceeded rate limitation while asking for rate "
                            + "limit status, suggesting 15 min");
                    return 15 * 60;
                } else if (e.isCausedByNetworkIssue()) {
                    logger.error("Network issues:", e);
                    netSleeper.sleep();
                }
            }
        }
    }
    
    private void waitForReset(String name) {
        int delay = findResetDelay(name);
        logger.info("Waiting for reset {}s (+5)", delay);
        try {
            TimeUnit.SECONDS.sleep(delay + 5);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting for limit reset");
            Thread.currentThread().interrupt();
        }
    }

    private void printRateLimits() {
        try {
            Map<String, RateLimitStatus> limits = twitter.getRateLimitStatus();
            for (Entry<String, RateLimitStatus> e : limits.entrySet()) {
                String name = e.getKey();
                RateLimitStatus status = e.getValue();

                int limit = status.getLimit();
                int remaining = status.getRemaining();
                int resetTime = status.getResetTimeInSeconds();
                int toReset = status.getSecondsUntilReset();

                if (remaining < limit) {
                    logger.info("{}: {}/{}, reset in {} (reset time = {})",
                            name, remaining, limit, toReset, resetTime);
                }
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }
    
    private List<Status> getUserStatuses(long userId) {
        Sleeper netSleeper = new Sleeper(30, 1);
        Sleeper unknownSleeper = new Sleeper(10, 1);
        int unknownCount = 0;
        int maxUnknownRetries = 3;

        while (true) {
            try {
                return twitter.getUserTimeline(userId);
            } catch (TwitterException e) {
                if (e.exceededRateLimitation()) {
                    logger.error("Exceeded rate limitation");
                    printRateLimits();
                    waitForReset("/statuses/user_timeline");
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
                    } else {
                        logger.error("Unknown error while fetching user", e);
                    }
                    ++ unknownCount;
                    logger.error("Unknown error {}/{}", unknownCount, 
                            maxUnknownRetries);
                    logger.error("Waiting {} s ...", unknownSleeper.getDelay());
                    unknownSleeper.sleep();
                    
                    if (unknownCount >= maxUnknownRetries) {
                        logger.error("Max of {} retries in face of unknown "
                                + "error reached, skipping user {}", 
                                maxUnknownRetries, userId);
                        throw new RuntimeException(e);
                    }
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
        beginSession();
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
