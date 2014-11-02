package pl.edu.agh.ed.twitter;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import pl.edu.agh.ed.twitter.domain.Tweet;


@Profile("FetchFFRetweets")
@Component
public class FetchFFRetweets extends AbstractRetweetsFetcher {

    @Override
    protected String flagPattern() {
        return "*%";
    }

    @Override
    protected void postProcessOriginalTweet(Tweet tweet) {
        tweet.setGotRetweets(true);
        consumed();
    }

    @Override
    protected void postProcessRetweet(Tweet retweet, Tweet orig) {
        retweet.addFlag(Tweet.FF_RETWEET);
        retweet.setLevel(1);
    }

}
