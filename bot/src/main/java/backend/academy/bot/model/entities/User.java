package backend.academy.bot.model.entities;

import java.util.List;

public class User {

    private Long id;

    private Long telegramId;

    private String username;

    private List<Link> trackingLinks;

    public User() {}

    public List<Link> getTrackingLinks() {
        return trackingLinks;
    }

    public void setTrackingLinks(List<Link> trackingLinks) {
        this.trackingLinks = trackingLinks;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User(Long telegramId, String username, List<Link> trackingLinks) {
        this.telegramId = telegramId;
        this.username = username;
        this.trackingLinks = trackingLinks;
    }
}
