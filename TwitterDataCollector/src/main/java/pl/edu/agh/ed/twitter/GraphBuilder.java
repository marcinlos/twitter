package pl.edu.agh.ed.twitter;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

@Configuration
@ComponentScan(basePackages = "pl.edu.agh.ed.twitter")
@ImportResource({
    "classpath:spring.xml",
})
@EnableScheduling
public class GraphBuilder {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(GraphBuilder.class, args);
        Runnable fetcher = ctx.getBean(FetchRecommended.class);
        
        fetcher.run();
//        Twitter twitter = TwitterFactory.getSingleton();
        
    }
    
    @Bean
    public Twitter getTwitter() {
        return TwitterFactory.getSingleton();
    }
}