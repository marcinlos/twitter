package pl.edu.agh.ed.twitter;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import pl.edu.agh.ed.twitter.domain.Tweet;
import pl.edu.agh.ed.twitter.domain.User;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

@Component
public class TweetListener implements StatusListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<Long> toBeDeleted = new HashSet<>();

    @Autowired
    private TweetDAO tweets;

    @Autowired
    private UserDAO users;

    @Override
    public void onException(Exception e) {
        logger.error("Problem", e);
    }

    @Override
    @Transactional
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        long id = statusDeletionNotice.getStatusId();
        logger.info("Deletion notice [id={}]", id);

        Tweet tweet = tweets.get(id);
        if (tweet != null) {
            tweets.delete(tweet);
        } else {
            toBeDeleted.add(id);
        }
    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {
        logger.info("Request to remove geographic data for {} up to [id={}]",
                userId, upToStatusId);
    }

    @Override
    public void onStallWarning(StallWarning warning) {
        logger.info("Stall warning: queue {}% full", warning.getPercentFull());
    }

    @Override
    @Transactional
    public void onStatus(Status status) {
        long userId = status.getUser().getId();
        long tweetId = status.getId();

        if (toBeDeleted.contains(tweetId)) {
            toBeDeleted.remove(tweetId);
        } else {
            User user = users.get(userId);
            if (user == null) {
                user = User.fromUser(status.getUser());
                users.save(user);
            }
            Tweet tweet = Tweet.fromStatus(status, user);
            tweets.update(tweet);
        }
    }

    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        logger.warn("Limitation notice: {}", numberOfLimitedStatuses);
    }

}