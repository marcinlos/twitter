package pl.edu.agh.ed.twitter.util;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sleeper {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private int delay;
    private int mult;
    
    public Sleeper(int seconds, int mult) {
        this.delay = seconds;
        this.mult = mult;
    }
    
    public void sleep() {
        try {
            TimeUnit.SECONDS.sleep(delay);
            delay *= mult;
        } catch (InterruptedException e) {
            logger.error("During waiting", e);
        }
    }
}