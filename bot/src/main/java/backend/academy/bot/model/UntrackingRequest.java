package backend.academy.bot.model;

public class UntrackingRequest {
    private String link;
    private long userId;

    public UntrackingRequest() {
    }

    public UntrackingRequest(String link, long userId) {
        this.link = link;
        this.userId = userId;
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
}
