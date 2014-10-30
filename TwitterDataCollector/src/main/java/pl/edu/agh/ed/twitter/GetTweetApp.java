package pl.edu.agh.ed.twitter;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class GetTweetApp {

    // https://api.twitter.com/1.1/statuses/show.json?id=

    public GetTweetApp() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {

        final Twitter twitter = new TwitterFactory().getInstance();
        try {
            Status status = twitter.showStatus(Long
                    .parseLong("523125374104776704"));
            if (status == null) { //
                // don't know if needed - T4J docs are VERY BAD
            } else {
                System.out.println("@" + status.getUser().getScreenName()
                        + " - " + status.getText() + "\nRetweets: "
                        + status.getRetweetCount() + "\nFavourites: "
                        + status.getFavoriteCount());
            }
        } catch (TwitterException e) {
            System.err.print("Failed to search tweets: " + e.getMessage());
            // e.printStackTrace();
            // DON'T KNOW IF THIS IS THROWN WHEN ID IS INVALID
        }

    }

}
