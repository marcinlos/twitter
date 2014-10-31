package pl.edu.agh.ed.twitter;

import org.springframework.context.annotation.Bean;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class BaseApp {

    @Bean
    public Twitter getTwitter() {
        return TwitterFactory.getSingleton();
    }

}
