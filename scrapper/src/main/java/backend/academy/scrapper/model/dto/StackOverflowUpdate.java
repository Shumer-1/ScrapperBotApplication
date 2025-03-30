package backend.academy.scrapper.model.dto;

import java.time.Instant;

public class StackOverflowUpdate {
    private final String questionTitle;
    private final String author;
    private final Instant creationTime;
    private final String bodyPreview;

    public StackOverflowUpdate(String questionTitle, String author, Instant creationTime, String bodyPreview) {
        this.questionTitle = questionTitle;
        this.author = author;
        this.creationTime = creationTime;
        this.bodyPreview = bodyPreview;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public String getAuthor() {
        return author;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public String getBodyPreview() {
        return bodyPreview;
    }
}
