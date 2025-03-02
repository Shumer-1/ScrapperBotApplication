package backend.academy.scrapper.model;

import java.time.Instant;

public class TrackingData {
    private String link;
    private long userId;
    private String[] tags;
    private String[] filters;
    private Instant lastUpdated;

    public TrackingData() {
    }

    public TrackingData(String link, long userId, String[] tags, String[] filters, Instant lastUpdated) {
        this.link = link;
        this.userId = userId;
        this.tags = tags;
        this.filters = filters;
        this.lastUpdated = lastUpdated;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String[] getFilters() {
        return filters;
    }

    public void setFilters(String[] filters) {
        this.filters = filters;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

}
