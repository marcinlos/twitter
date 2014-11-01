package pl.edu.agh.ed.twitter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.ConfigurationBuilder;


@Component
public class TwitterProvider {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Twitter makeAppAuth(String key, String secret) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(key);
        cb.setOAuthConsumerSecret(secret);
        cb.setApplicationOnlyAuthEnabled(true);

        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        
        try {
            twitter.getOAuth2Token();
        } catch (TwitterException e) {
            logger.error("Cannot obtain bearer token!", e);
            logger.error("Key    = {}", key);
            logger.error("Secret = {}", secret);
            return null;
        }
        return twitter;
    }
    
    private Twitter makeUserAuth(String key, String secret, String accessKey,
            String accessSecret) {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(key);
        cb.setOAuthConsumerSecret(secret);
        cb.setOAuthAccessToken(accessKey);
        cb.setOAuthAccessTokenSecret(accessSecret);
        
        OAuthAuthorization auth = new OAuthAuthorization(cb.build());
        
        Twitter twitter = new TwitterFactory().getInstance(auth);
        return twitter;
    }

    public List<Twitter> getTwitters() {
        
        String key = "xbh7939LQO1TmiTfCIiL1w";
        String secret = "c9wqs3it25ZIL4be9aRFekbp3xR2uptFLVhs10Q67o";
        String accessKey = "2180587321-ODN7s9mZY6VylifdeySxlAYDMLyDm0Ooe4eqDVy";
        String accessSecret = "D3gvCYGR0nKWUmSkhJnqvAZu2FjKFTpUJR1CnppQvn2qs";
        
        String key2 = "zARgGE4lzFuGISoDY28gRYH05";
        String secret2 = "bNGWpYmupcGnbVSSeMZA3NncSTehlBXxOXaZlZzVuZEZoh54m9";
        String accessKey2 = "2180587321-UJ1Xnv2yyhd0AmGppottXFe2DEBMd9QG74sXfJc";
        String accessSecret2 = "P7A7Pn3At9jrsMfAleMmuUPWeVOTi9fQCM1GO8irj7QmS";
        
        String key3 = "SVcfTsfez3BteZ9E0OZu01sT9";
        String secret3 = "PHGxvUde8gU9oz6Ub27QM1GgHWboskK98G6s4bALuPL2RZ01A6";
        String accessKey3 = "2854222545-EJXp1aZRGGirdnK1nDJTvDl3BfUIFJ5EgRAdmyd";
        String accessSecret3 = "WmoWqRtJ96kxNoSm4KKLZ9pviUgfNLeA0LG8GS3WAxsS9";
        
        Twitter[] twitters = {
            makeAppAuth(key, secret),
            makeAppAuth(key2, secret2),
            makeAppAuth(key3, secret3),
            makeUserAuth(key, secret, accessKey, accessSecret),
            makeUserAuth(key2, secret2, accessKey2, accessSecret2),
            makeUserAuth(key3, secret3, accessKey3, accessSecret3)
        };
        
        List<Twitter> list = new ArrayList<>();
        
        int n = 0;
        for (Twitter twitter : twitters) {
            if (twitter != null) {
                list.add(twitter);
            } else {
                logger.warn("Twitter {} is null", n);
            }
            ++ n;
        }

        return list;
    }

}
