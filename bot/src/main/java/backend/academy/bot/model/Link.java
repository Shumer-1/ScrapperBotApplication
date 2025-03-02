package backend.academy.bot.model;

import java.util.List;

public class Link {

    private long id;
    private String link;
    private long userId;
    private List<String> tags;
    private List<String> filters;

    public Link(String link, long userId, long id, List<String> tags, List<String> filters) {
        this.tags = tags;
        this.link = link;
        this.userId = userId;
        this.id = id;
        this.filters = filters;
    }

    public long id() {
        return this.id;
    }

    public String link() {
        return this.link;
    }

    public long userId() {
        return this.userId;
    }

    public List<String> tags() {
        return this.tags;
    }

    public List<String> filters() {
        return this.filters;
    }

    public Link id(long id) {
        this.id = id;
        return this;
    }

    public Link link(String link) {
        this.link = link;
        return this;
    }

    public Link userId(long userId) {
        this.userId = userId;
        return this;
    }

    public Link tags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public Link filters(List<String> filters) {
        this.filters = filters;
        return this;
    }
}
