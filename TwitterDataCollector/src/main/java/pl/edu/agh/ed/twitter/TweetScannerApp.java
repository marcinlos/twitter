package pl.edu.agh.ed.twitter;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import twitter4j.FilterQuery;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

@Configuration
@ComponentScan(basePackages = "pl.edu.agh.ed.twitter")
@ImportResource({ "classpath:spring.xml" })
@EnableScheduling
public class TweetScannerApp {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(TweetScannerApp.class, args);
        StatusListener listener = ctx.getBean(StatusListener.class);

        TwitterStream stream = TwitterStreamFactory.getSingleton();
        stream.addListener(listener);

        FilterQuery filter = new FilterQuery();
        filter.track(new String[]{"#FF"});
        filter.language(new String[] {"en"});
        stream.filter(filter);
    }
}