package pl.edu.agh.ed.twitter.dao;

import org.springframework.stereotype.Repository;

import pl.edu.agh.ed.twitter.domain.Tweet;

@Repository
public class TweetDAO extends BaseDAO<Long, Tweet> {
    
    @Override
    protected Class<Tweet> getEntityClass() {
        return Tweet.class;
    }

}
