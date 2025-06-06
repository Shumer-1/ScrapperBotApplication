package backend.academy.scrapper.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tg_id", nullable = false, unique = true)
    @NotNull
    private Long telegramId;

    @Column(name = "username", nullable = false)
    @NotNull
    private String username;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    @NotNull
    private List<Link> trackingLinks = new ArrayList<>();

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
