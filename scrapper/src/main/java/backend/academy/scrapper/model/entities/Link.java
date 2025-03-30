package backend.academy.scrapper.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "tracking_link")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "link", nullable = false)
    private String link;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany
    @JoinTable(
            name = "link_and_tags",
            joinColumns = @JoinColumn(name = "link_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;

    @ManyToMany
    @JoinTable(
            name = "link_and_filters",
            joinColumns = @JoinColumn(name = "link_id"),
            inverseJoinColumns = @JoinColumn(name = "filter_id"))
    private Set<Filter> filters;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    public Link() {}

    public Link(String link, User user, Set<Tag> tags, Set<Filter> filters, Instant lastUpdated) {
        this.link = link;
        this.user = user;
        this.tags = tags;
        this.filters = filters;
        this.lastUpdated = lastUpdated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Set<Filter> getFilters() {
        return filters;
    }

    public void setFilters(Set<Filter> filters) {
        this.filters = filters;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
