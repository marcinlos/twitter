package pl.edu.agh.ed.twitter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import pl.edu.agh.ed.twitter.domain.Tweet;


@Component
public class FetchRecommendedImpl implements FetchRecommended {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TweetDAO tweets;
    
    @Autowired
    private UserDAO users;
    
    private final static Pattern pat = Pattern.compile("@\\w+");
    
    private static final Criterion FF = Restrictions.like("flag", "*%");
    private static final Criterion NOT_DONE = Restrictions.eq("gotRetweet", false);
    private static final Criterion TO_FETCH = Restrictions.and(FF, NOT_DONE);

    private static final int CHUNK_SIZE = 100;
    
    private int first = 0;
    
    private static List<String> extractRecommended(String text) {
        List<String> recommended = new ArrayList<>();
        Matcher m = pat.matcher(text);
        while (m.find()) {
            recommended.add(m.group());
        }
        return recommended;
    }
    
    @Override
    public void run() {
        List<Tweet> chunk = nextChunk();
        while (! chunk.isEmpty()) {
            logger.info("Chunk {}-{}", first - chunk.size(), first - 1);
            for (Tweet tweet : chunk) {
                try {
                    process(tweet);
                } catch (Exception e) {
                    logger.error("During FF tweet processing", e);
                }
            }
            chunk = nextChunk();
        }
    }

    private void process(Tweet tweet) {
        String text = tweet.getText();
        List<String> recommended = extractRecommended(text);
        logger.info("Tweet [id={}]:", tweet.getId());
        for (String user : recommended) {
            logger.info("    -{}", user);
        }
    }
    
    @Transactional
    public List<Tweet> nextChunk() {
        List<Tweet> list = tweets.getList(first, CHUNK_SIZE, TO_FETCH);
        first += list.size();
        return list;
    }

}
