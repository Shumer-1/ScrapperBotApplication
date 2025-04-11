package backend.academy.scrapper.model.dto;

import java.time.Instant;

public record StackOverflowUpdate(String questionTitle, String author,
                                  Instant creationTime, String bodyPreview) {}
