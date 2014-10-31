package pl.edu.agh.ed.twitter.domain;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import twitter4j.Place;
import twitter4j.Status;


@Entity
public class Tweet {
    
    @Id
    private Long id;
    
    @Column
    private String text;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column
    private Date createdAt;
    
    @Column
    private String source;
    
    @Column
    private long inReplyToStatus;
    
    @Column
    private long inReplyToUser;
    
    @Column
    private long retweetedStatus;
    
    @Column
    private long retweetCount;
    
    @Column
    private long favoriteCount;
    
    @Column
    private String language;

    @Column
    private String country;
    
    @Column(length = 6)
    private String flag = "......";
    
    @Column
    private int level;
    
    @Column
    private int ffLevel;
    
    @Column
    private boolean gotRetweets = false;
    
    @Column
    private boolean gotRecommended = false;
    
    public static final char MARK = '*';
    
    public static final int FF                     = 0;
    public static final int BY_FF_TWEETER          = 1;
    public static final int FF_RETWEET             = 2;
    public static final int RETWEET_OF_FF_TWEETER  = 3;
    public static final int BY_RECOMMENDED         = 4;
    public static final int RETWEET_OF_RECOMMENDED = 5;
    
    public int getFFLevel() {
        return ffLevel;
    }
    
    public void setFFLevel(int level) {
        this.ffLevel = level;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public void addFlag(int flag) {
        char[] cs = this.flag.toCharArray();
        cs[flag] = MARK;
        this.flag = String.valueOf(cs);
    }
    
    public boolean checkFlag(int flag) {
        return this.flag.charAt(flag) == MARK;
    }
    
    public boolean isFF() {
        return checkFlag(FF);
    }
    
    public void markFF() {
        addFlag(FF);
    }
    
    public boolean isByFFTweeter() {
        return checkFlag(BY_FF_TWEETER);
    }
    
    public void markByFFTweeter() {
        addFlag(BY_FF_TWEETER);
    }
    
    public boolean isFFRetweet() {
        return checkFlag(FF_RETWEET);
    }
    
    public void markFFRetweet() {
        addFlag(FF_RETWEET);
    }
    
    public boolean isRetweetOfFFTweeter() {
        return checkFlag(RETWEET_OF_FF_TWEETER);
    }
    
    public void markRetweetOfFFTweeter() {
        addFlag(RETWEET_OF_FF_TWEETER);
    }
    
    public boolean isByRecommended() {
        return checkFlag(BY_RECOMMENDED);
    }
    
    public void markByRecommended() {
        addFlag(BY_RECOMMENDED);
    }
    
    public boolean isRetweetOfRecommended() {
        return checkFlag(RETWEET_OF_RECOMMENDED);
    }
    
    public void markRetweetOfRecommended() {
        addFlag(RETWEET_OF_RECOMMENDED);
    }
    

    public Tweet() {
        // empty parameterless ctor for Hibernate
    }
    
    public static Tweet fromStatus(Status status, User user) {
        Tweet tweet = new Tweet();

        tweet.id = status.getId();
        tweet.text = status.getText();
        tweet.user = user;
        tweet.createdAt = new Date(status.getCreatedAt().getTime());
        tweet.source = status.getSource();
        tweet.inReplyToStatus = status.getInReplyToStatusId();
        tweet.inReplyToUser = status.getInReplyToUserId();
        if (status.isRetweet()) {
            tweet.retweetedStatus = status.getRetweetedStatus().getId();
        }
        tweet.retweetCount = status.getRetweetCount();
        tweet.favoriteCount = status.getFavoriteCount();
        tweet.language = status.getLang();
        
        if (status.getPlace() != null) {
            Place place = status.getPlace();
            tweet.country = place.getName();
        }
        
        return tweet;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getInReplyToStatus() {
        return inReplyToStatus;
    }

    public void setInReplyToStatus(long inReplyToStatus) {
        this.inReplyToStatus = inReplyToStatus;
    }

    public long getInReplyToUser() {
        return inReplyToUser;
    }

    public void setInReplyToUser(long inReplyToUser) {
        this.inReplyToUser = inReplyToUser;
    }

    public long getRetweetedStatus() {
        return retweetedStatus;
    }
    
    public void setRetweetedStatus(long retweetedStatus) {
        this.retweetedStatus = retweetedStatus;
    }

    public long getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(long retweetCount) {
        this.retweetCount = retweetCount;
    }

    public long getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
    
    public boolean isRetweet() {
        return retweetedStatus != 0;
    }

    public boolean isReplyToStatus() {
        return inReplyToStatus != -1;
    }
    
    public boolean isReplyToUser() {
        return inReplyToUser != -1;
    }
    
    public boolean hasGotRetweets() {
        return gotRetweets;
    }
    
    public void setGotRetweets(boolean got) {
        gotRetweets = got;
    }
    
    public boolean hasGotRecommended() {
        return gotRecommended;
    }
    
    public void setGotRecommended(boolean got) {
        gotRecommended = got;
    }
    
}
