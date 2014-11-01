package pl.edu.agh.ed.twitter.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;

public class TwitterManager {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final Map<Twitter, LimitRateHelper> helpers = new HashMap<>();
    
    public TwitterManager(List<Twitter> twitters) {
        for (Twitter twitter : twitters) {
            helpers.put(twitter, new LimitRateHelper(twitter));
        }
    }
    
    private Map<Twitter, RateLimitStatus> getLimits(String call) {
        Map<Twitter, RateLimitStatus> limits = new HashMap<>();
        
        for (Entry<Twitter, LimitRateHelper> e : helpers.entrySet()) {
            Twitter twitter = e.getKey();
            LimitRateHelper helper = e.getValue();
            
            limits.put(twitter, helper.getLimits().get(call));
        }
        return limits;
    }
    
    public Twitter getFor(String call) {
        Map<Twitter, RateLimitStatus> limits = getLimits(call);
        int most = 0;
        int delay = Integer.MIN_VALUE;
        Twitter best = null;
        
        for (Entry<Twitter, RateLimitStatus> e : limits.entrySet()) {
            Twitter twitter = e.getKey();
            RateLimitStatus status = e.getValue();
            boolean better = false;
            
            if (best == null) {
                better = true;
            } else if (status.getSecondsUntilReset() < delay) {
                better = true;
            } else if (status.getRemaining() > most) {
                better = true;
            }
            if (better) {
                best = twitter;
                most = status.getRemaining();
                delay = most > 0 ? 0 : status.getSecondsUntilReset();
            }
        }
        
        logger.info("Using {} for {}", best, call);
        
        if (delay > 0) {
            helpers.get(best).waitForReset(delay);
        }
        return best;
    }
    
}
