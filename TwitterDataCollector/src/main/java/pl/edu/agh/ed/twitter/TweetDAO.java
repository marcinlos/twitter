package pl.edu.agh.ed.twitter;

import java.util.List;

import org.springframework.stereotype.Repository;

import pl.edu.agh.ed.twitter.domain.Tweet;

@Repository
public class TweetDAO extends BaseDAO<Long, Tweet> {
    
    public List<Tweet> getTweets() {
        return (List<Tweet>) session().createQuery("from Tweet").list();
    }
    
    

    @Override
    protected Class<Tweet> getEntityClass() {
        return Tweet.class;
    }

}
