package pl.edu.agh.ed.twitter.dao;

import org.springframework.stereotype.Repository;

import pl.edu.agh.ed.twitter.domain.Recommendation;
import pl.edu.agh.ed.twitter.domain.RecommendationID;


@Repository
public class RecommendationDAO extends BaseDAO<RecommendationID, Recommendation> {

    @Override
    protected Class<Recommendation> getEntityClass() {
        return Recommendation.class;
    }

}
