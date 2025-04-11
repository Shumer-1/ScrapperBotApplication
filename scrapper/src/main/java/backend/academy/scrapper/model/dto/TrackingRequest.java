package backend.academy.scrapper.model.dto;

import java.util.List;

public record TrackingRequest(String link, long userId,
                              List<String> tags, List<String> filters) {}
