package backend.academy.scrapper.service.sourceHandlers;

import backend.academy.scrapper.model.entities.Link;
import reactor.core.publisher.Mono;

public interface SourceHandler {
    boolean canHandle(String link);

    Mono<Void> process(Link link);
}
