package backend.academy.bot.model;

import java.util.List;

public class TrackingRequest {
    private String link;
    private long userId;
    private List<String> tags;
    private List<String> filters;

    public TrackingRequest() {
    }

    public TrackingRequest(String link, long userId, List<String> tags, List<String> filters) {
        this.link = link;
        this.userId = userId;
        this.tags = tags;
        this.filters = filters;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }
}
