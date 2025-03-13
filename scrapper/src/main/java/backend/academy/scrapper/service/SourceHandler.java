package backend.academy.scrapper.service;

import backend.academy.scrapper.model.TrackingData;
import reactor.core.publisher.Mono;

public interface SourceHandler {
    boolean canHandle(String link);

    Mono<Void> process(TrackingData trackingData);
}
