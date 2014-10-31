package pl.edu.agh.ed.twitter.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class RecommendationID implements Serializable {
    
    @Column
    private long tweetId;
    
    @Column
    private long userId;
    
    public RecommendationID() {
        // empty ctor
    }

    public RecommendationID(long tweetId, long userId) {
        this.tweetId = tweetId;
        this.userId = userId;
    }
    
    public RecommendationID(Tweet tweet, User user) {
        this(tweet.getId(), user.getId());
    }

    public long getTweet() {
        return tweetId;
    }

    public void setTweet(long tweetId) {
        this.tweetId = tweetId;
    }

    public long getUser() {
        return userId;
    }

    public void setUser(long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RecommendationID) {
            RecommendationID id = (RecommendationID) o;
            return tweetId == id.tweetId && userId == id.userId;
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(tweetId, userId);
    }
}
