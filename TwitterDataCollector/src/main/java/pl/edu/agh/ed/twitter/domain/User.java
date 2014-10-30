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

}
