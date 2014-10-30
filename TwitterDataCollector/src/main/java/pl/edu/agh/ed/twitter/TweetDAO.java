package pl.edu.agh.ed.twitter;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pl.edu.agh.ed.twitter.domain.Tweet;

@Repository
@Transactional
public class TweetDAO extends BaseDAO {
    
    public List<Tweet> getTweets() {
        return (List<Tweet>) session().createQuery("from Tweet").list();
    }

}
