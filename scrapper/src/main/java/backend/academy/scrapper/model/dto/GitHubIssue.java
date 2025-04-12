package backend.academy.scrapper.model.dto;

import java.time.Instant;

public record GitHubIssue(String title, String username, Instant createdAt, String bodyPreview) {}
