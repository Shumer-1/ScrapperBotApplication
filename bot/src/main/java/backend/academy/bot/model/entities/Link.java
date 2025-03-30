package backend.academy.bot.model.entities;

import java.util.Set;

public class Link {

    private Long id;

    private String link;

    private User user;

    private Set<Tag> tags;

    private Set<Filter> filters;

    public Link() {}

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

    public Link(String link, User user, Set<Tag> tags, Set<Filter> filters) {
        this.link = link;
        this.user = user;
        this.tags = tags;
        this.filters = filters;
    }
}
