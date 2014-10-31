package pl.edu.agh.ed.twitter.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;


@Entity
public class Recommendation {

    @EmbeddedId
    private RecommendationID id;
    
    public Recommendation() {
        // empty ctor
    }
    
    public Recommendation(Tweet tweet, User user) {
        this.id = new RecommendationID(tweet, user);
    }

    public RecommendationID getId() {
        return id;
    }

    public void setId(RecommendationID id) {
        this.id = id;
    }

}
