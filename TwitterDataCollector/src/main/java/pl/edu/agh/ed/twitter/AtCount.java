package pl.edu.agh.ed.twitter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import pl.edu.agh.ed.twitter.domain.Tweet;


public class AtCount {
    
    private final static Pattern pat = Pattern.compile("@\\w+");
            
    private static int getAtCount(String text) {
        Matcher m = pat.matcher(text);
        int n = 0;
        while (m.find()) {
            ++ n;
        }
        return n;
    }

    public static void main(String[] args) {
        
        
        ApplicationContext container = new ClassPathXmlApplicationContext("spring.xml");
        TweetDAO dao = container.getBean(TweetDAO.class);
        StatelessSession session = dao.sessionFactory().openStatelessSession();
//        Transaction tx = session.beginTransaction();

        System.out.println("Before query");
        ScrollableResults res = session.createQuery("from Tweet").scroll(ScrollMode.FORWARD_ONLY);
        System.out.println("After query");
        Map<Integer, Integer> counts = new HashMap<>();
        int done = 0;
        
        while (res.next()) {
            Tweet tweet = (Tweet) res.get(0);
            int count = getAtCount(tweet.getText());
            Integer n = counts.get(count);
            if (n == null) {
                n = 0;
            }
            counts.put(count, n + 1);
            ++ done;
            if (done % 100 == 0) {
                System.out.println("Done: " + done);
            }
        }
        
//        tx.commit();
        System.out.println("Done with iterations");
        
        for (Entry<Integer, Integer> e : counts.entrySet()) {
            System.out.printf("%s\t%s\n", e.getKey(), e.getValue());
        }
        
    }
}