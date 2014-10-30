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

    public Tweet() {
        // empty parameterless ctor for Hibernate
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

}
