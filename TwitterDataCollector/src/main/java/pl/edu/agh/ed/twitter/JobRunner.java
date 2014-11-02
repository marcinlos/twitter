package pl.edu.agh.ed.twitter;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

@Configuration
@ComponentScan(basePackages = "pl.edu.agh.ed.twitter")
@ImportResource({ "classpath:spring.xml" })
@Profile({ 
    "FF", 
    "recommended", 
    "FetchFFRetweets" 
})
public class JobRunner {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(JobRunner.class, args);
        Job job = ctx.getBean(Job.class);
        job.run();
    }

    @Bean
    public Twitter getTwitter() {
        return TwitterFactory.getSingleton();
    }
}