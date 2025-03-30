package backend.academy.scrapper.model.dto;

import java.time.Instant;

public class GitHubIssue {
    private final String title;
    private final String username;
    private final Instant createdAt;
    private final String bodyPreview;

    public GitHubIssue(String title, String username, Instant createdAt, String bodyPreview) {
        this.title = title;
        this.username = username;
        this.createdAt = createdAt;
        this.bodyPreview = bodyPreview;
    }

    public String getTitle() {
        return title;
    }

    public String getUsername() {
        return username;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getBodyPreview() {
        return bodyPreview;
    }
}
