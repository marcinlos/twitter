package pl.edu.agh.ed.twitter.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;


@Component
public class LimitRateHelper {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    public enum Limits {
        ALL,
        USED,
        DEPLETED
    }

    @Autowired
    private Twitter twitter;
    
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
    
    public void waitForReset(String name) {
        int delay = findResetDelay(name);
        logger.info("Waiting for reset {}s (+5)", delay);
        try {
            TimeUnit.SECONDS.sleep(delay + 5);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting for limit reset");
            Thread.currentThread().interrupt();
        }
    }
    
    public void printRateLimits() {
        printRateLimits(Limits.USED);
    }

    public void printRateLimits(Limits which) {
        try {
            Map<String, RateLimitStatus> limits = twitter.getRateLimitStatus();
            for (Entry<String, RateLimitStatus> e : limits.entrySet()) {
                String name = e.getKey();
                RateLimitStatus status = e.getValue();

                int limit = status.getLimit();
                int remaining = status.getRemaining();
                int resetTime = status.getResetTimeInSeconds();
                int toReset = status.getSecondsUntilReset();
                
                boolean show = true;
                if (which == Limits.USED) {
                    show = remaining < limit;
                } else if (which == Limits.DEPLETED) {
                    show = remaining == 0;
                }

                if (show) {
                    logger.info("{}: {}/{}, reset in {} (reset time = {})",
                            name, remaining, limit, toReset, resetTime);
                }
            }
        } catch (TwitterException e) {
            logger.error("Twitter error", e);
        }
    }
    
}
