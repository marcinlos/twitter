package pl.edu.agh.ed.twitter;

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
    
    @Autowired
    private TweetDAO tweets;
    
    @Autowired
    private UserDAO users;
    
    @Override
    public void onException(Exception e) {
        System.err.println("Problem:");
        e.printStackTrace(System.err);
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        
    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {
        
    }

    @Override
    public void onStallWarning(StallWarning warning) {
        System.out.println("Stall warning: " + warning);
    }

    @Override
    @Transactional
    public void onStatus(Status status) {
//        printTweet(status);
        long userId = status.getUser().getId();
        User user = users.getUser(userId);
        if (user == null) {
            user = User.fromUser(status.getUser());
            users.save(user);
        }
        Tweet tweet = Tweet.fromStatus(status, user);
        tweets.save(tweet);
    }

    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        System.out.println("Limitation notice - " + numberOfLimitedStatuses);
    }
    
}