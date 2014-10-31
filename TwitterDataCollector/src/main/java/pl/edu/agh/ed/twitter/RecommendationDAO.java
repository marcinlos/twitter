package pl.edu.agh.ed.twitter;

import pl.edu.agh.ed.twitter.domain.Recommendation;
import pl.edu.agh.ed.twitter.domain.RecommendationID;

public class RecommendationDAO extends BaseDAO<RecommendationID, Recommendation> {

    @Override
    protected Class<Recommendation> getEntityClass() {
        return Recommendation.class;
    }

}
