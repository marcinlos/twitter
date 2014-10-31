package pl.edu.agh.ed.twitter;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ComponentScan(basePackages = "pl.edu.agh.ed.twitter")
@ImportResource({ "classpath:spring.xml" })
public class RecommendedFetcherApp extends BaseApp {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(RecommendedFetcherApp.class, args);
        Runnable fetcher = ctx.getBean(FetchRecommended.class);
        
        fetcher.run();
    }

}