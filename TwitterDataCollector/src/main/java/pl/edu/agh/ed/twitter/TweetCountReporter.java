package pl.edu.agh.ed.twitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class TweetCountReporter {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TweetDAO tweets;
    
    @Autowired
    private UserDAO users;

    @Scheduled(fixedRate = 10 * 1000)
    @Transactional
    public void report() {
        long tweetCount = tweets.count();
        long userCount = users.count();
        logger.info("Tweets: {}, users: {}", tweetCount, userCount);
    }
    
}
