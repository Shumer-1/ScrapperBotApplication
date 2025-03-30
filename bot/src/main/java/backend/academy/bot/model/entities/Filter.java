package backend.academy.bot.model.entities;

import java.util.Set;

public class Filter {

    private Long id;

    private String filter;

    private Set<Link> trackingLinks;

    public Filter() {}

    public Set<Link> getTrackingLinks() {
        return trackingLinks;
    }

    public void setTrackingLinks(Set<Link> trackingLinks) {
        this.trackingLinks = trackingLinks;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Filter(String filter) {
        this.filter = filter;
    }
}
