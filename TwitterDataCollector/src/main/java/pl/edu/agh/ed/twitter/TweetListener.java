package pl.edu.agh.ed.twitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import pl.edu.agh.ed.twitter.domain.Tweet;
import pl.edu.agh.ed.twitter.domain.User;
import twitter4j.Place;
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
    
    private void printTweet(Status tweet) {
        System.out.println("- - - - - - - - - - - - - - - - - - -  - -");
        twitter4j.User user = tweet.getUser();
        System.out.println("Id: " + tweet.getId());
        System.out.println("At: " + tweet.getCreatedAt());
        System.out.println("Source: " + tweet.getSource());
        System.out.println("User: " + user.getScreenName() + " [id=" + user.getId() + "]");
        System.out.println("Text: \n<<<" + tweet.getText() + "\n>>>");

        if (! tweet.getLang().equals("und")) {
            System.out.println("Language: " + tweet.getLang());
        }
        if (tweet.getScopes() != null) {
            System.out.println("Scopes: " + tweet.getScopes());
        }
        if (tweet.isRetweet()) {
            Status retweeted = tweet.getRetweetedStatus();
            System.out.println("Is retweet of: " + retweeted.getId());
            System.out.println("               retweets: " + retweeted.getRetweetCount());
            System.out.println("               favorites: " + retweeted.getFavoriteCount());
        }
        System.out.println("Is retweeted: " + tweet.isRetweeted());
        System.out.println("Retweet count: " + tweet.getRetweetCount());
        System.out.println("Is favorited: " + tweet.isFavorited());
        System.out.println("Fav count: " + tweet.getFavoriteCount());
        
        System.out.println("In reply to status: " + tweet.getInReplyToStatusId());
        System.out.println("In reply to user: " + tweet.getInReplyToUserId());
        System.out.println("In reply to screen name: " + tweet.getInReplyToScreenName());
        
        Place place = tweet.getPlace();
        if (place != null) {
            System.out.println("Place:");
            System.out.println(" - name: " + place.getName());
            System.out.println(" - streetAddress: " + place.getStreetAddress());
            System.out.println(" - id: " + place.getId());
            System.out.println(" - country: " + place.getCountry());
            System.out.println(" - place type: " + place.getPlaceType());
            System.out.println(" - url: " + place.getURL());
            System.out.println(" - full name: " + place.getFullName());
        } else {
            System.out.println("[No place]");
        }
        System.out.println("- - - - - - - - - - - - - - - - - - -  - -");
    }
    
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