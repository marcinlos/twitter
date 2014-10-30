package pl.edu.agh.ed.twitter;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import twitter4j.FilterQuery;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
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
        ApplicationContext container = new ClassPathXmlApplicationContext("spring.xml");
        StatusListener listener = container.getBean(StatusListener.class);
//        TweetListener listener = new TweetListener(null);

        TwitterStream stream = TwitterStreamFactory.getSingleton();
        stream.addListener(listener);

        FilterQuery filter = new FilterQuery();
        filter.track(new String[]{"#FF"});
        stream.filter(filter);
    }
}