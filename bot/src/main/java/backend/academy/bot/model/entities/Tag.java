package backend.academy.bot.model.entities;

import java.util.HashSet;
import java.util.Set;

public class Tag {

    private Long id;

    private String tag;

    private Set<Link> trackingLinks;

    public Tag() {}

    public Tag(String tag) {
        this.tag = tag;
        this.trackingLinks = new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Set<Link> getTrackingLinks() {
        return trackingLinks;
    }

    public void setTrackingLinks(Set<Link> trackingLinks) {
        this.trackingLinks = trackingLinks;
    }

    public Tag(String tag, Set<Link> trackingLinks) {
        this.tag = tag;
        this.trackingLinks = trackingLinks;
    }
}
