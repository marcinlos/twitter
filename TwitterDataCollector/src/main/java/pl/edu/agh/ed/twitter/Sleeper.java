package pl.edu.agh.ed.twitter;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Sleeper {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private int delay;
    private int mult;
    
    public Sleeper(int seconds, int mult) {
        this.delay = seconds;
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