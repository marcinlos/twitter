package pl.edu.agh.ed.twitter;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan(basePackages = "pl.edu.agh.ed.twitter")
@ImportResource({ "classpath:spring.xml" })
@Profile({ "FF", "recommended" })
public class TweetFetcherApp extends BaseApp {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(TweetFetcherApp.class, args);
        Runnable fetcher = ctx.getBean(TweetFetcher.class);
        
        fetcher.run();
    }

}