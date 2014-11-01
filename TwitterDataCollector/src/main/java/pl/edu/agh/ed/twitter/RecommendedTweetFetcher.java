package pl.edu.agh.ed.twitter;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import pl.edu.agh.ed.twitter.domain.Tweet;
import pl.edu.agh.ed.twitter.domain.User;

@Profile("recommended")
@Component
public class RecommendedTweetFetcher extends BasicTweetFetcher implements
        TweetFetcher {

    @Override
    protected String flagPattern() {
        return ".*%";
    }

    @Override
    protected void processTweet(Tweet tweet, User user) {
        tweet.addFlag(Tweet.BY_RECOMMENDED);
        tweet.setLevel(0);
    }

    @Override
    protected void processUser(User user) {
        user.setGotTweets(true);
        consumed();
    }

}
