package pl.edu.agh.ed.twitter.domain;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class User {
    
    @Id
    private long id;
    
    @Column
    private String name;
    
    @Column
    private String screenName;
    
    @Column
    private String location;

    @Column
    private String url;
    
    @Column
    private int followers;
    
    @Column
    private int followings;
    
    @Column
    private int favourites;
    
    @Column
    private String language;
    
    @Column
    private int statuses;
    
    @Column
    private boolean isVerified;
    
    @Column
    private String timeZone;
    
    @Column
    private Date createdAt;
    
    @Column
    private String backgroundColor;
    
    @Column
    private String textColor;
    
    @Column(length = 5)
    private String flag = "....."; 
    
    @Column
    private int level;
    
    @Column
    private boolean gotTweets;
    
    public static final char MARK = '*';
    
    public static final int FF_TWEETER            = 0;
    public static final int RECOMMENDED           = 1;
    public static final int FF_RETWEETER          = 2;
    public static final int RETWEETER             = 3;
    public static final int RECOMMENDED_RETWEETER = 4;
    
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
    
    public boolean isFFTweeter() {
        return checkFlag(FF_TWEETER);
    }
    
    public boolean isRecommended() {
        return checkFlag(RECOMMENDED);
    }
    
    public boolean isFFRetweeter() {
        return checkFlag(FF_RETWEETER);
    }
    
    public boolean isRetweeter() {
        return checkFlag(RETWEETER);
    }
    
    public boolean isRecommendedRetweeter() {
        return checkFlag(RECOMMENDED_RETWEETER);
    }
    
    public void markFFTweeter() {
        addFlag(FF_TWEETER);
    }

    public void markRecommended() {
        addFlag(RECOMMENDED);
    }
    
    public void markFFRetweeter() {
        addFlag(FF_RETWEETER);
    }
    
    public void markRetweeter() {
        addFlag(RETWEETER);
    }
    
    public void markRecommendedRetweeter() {
        addFlag(RECOMMENDED_RETWEETER);
    }
    

    public User() {
        // parameterless ctor for Hibernate
    }
    
    public static User fromUser(twitter4j.User user) {
        User u = new User();
        u.id = user.getId();
        u.name = user.getName();
        u.screenName = user.getScreenName();
        u.location = user.getLocation();
        u.url = user.getURL();
        u.followers = user.getFollowersCount();
        u.followings = user.getFriendsCount();
        u.favourites = user.getFavouritesCount();
        u.language = user.getLang();
        u.statuses = user.getStatusesCount();
        u.isVerified = user.isVerified();
        u.timeZone = user.getTimeZone();
        u.createdAt = new Date(user.getCreatedAt().getTime());
        u.backgroundColor = user.getProfileBackgroundColor();
        u.textColor = user.getProfileTextColor();
        
        return u;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowings() {
        return followings;
    }

    public void setFollowings(int followings) {
        this.followings = followings;
    }

    public int getFavourites() {
        return favourites;
    }

    public void setFavourites(int favourites) {
        this.favourites = favourites;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getStatuses() {
        return statuses;
    }

    public void setStatuses(int statuses) {
        this.statuses = statuses;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
    
    public boolean hasGotTweets() {
        return gotTweets;
    }
    
    public void setGotTweets(boolean got) {
        gotTweets = got;
    }
    

}
