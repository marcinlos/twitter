package pl.edu.agh.ed.twitter;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;


class TweetScanner {
    
    private static final String FMT = "@%s - %s";
    
    private final Twitter twitter;
    
    public TweetScanner(Twitter twitter) {
        this.twitter = twitter;
    }
    
    public void search(String query) throws TwitterException {
        Query q = new Query(query);
        QueryResult result;
        
        do {
            result = twitter.search(q);
            List<Status> tweets = result.getTweets();
            for (Status tweet : tweets) {
                printTweet(tweet);
            }
        } while ((q = result.nextQuery()) != null);
    }
    
    private void printTweet(Status tweet) {
        User user = tweet.getUser();
        String text = String.format(FMT, user.getScreenName(), tweet.getText());
        System.out.println(text);
    }
    
}


public class Application {

    public static void main(String[] args) {
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            TweetScanner scanner = new TweetScanner(twitter);
            scanner.search("movies");
        } catch (TwitterException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to search tweets: " + e.getMessage());
        }
    }
}