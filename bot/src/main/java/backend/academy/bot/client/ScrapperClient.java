package backend.academy.bot.client;

import backend.academy.bot.model.TrackingRequest;
import backend.academy.bot.model.UntrackingRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ScrapperClient {

    private static final Logger log = LoggerFactory.getLogger(ScrapperClient.class);

    private final WebClient webClient;
    private final String scrapperBaseUrl;

    public ScrapperClient(WebClient webClient, @Value("${scrapper.base-url}") String scrapperBaseUrl) {
        this.webClient = webClient;
        this.scrapperBaseUrl = scrapperBaseUrl;
    }

    public Mono<Void> addTracking(String link, long userId, List<String> tags, List<String> filters) {
        TrackingRequest payload = new TrackingRequest(link, userId, tags, filters);
        log.info(
            "Отправка запроса на добавление отслеживания: действие={}, ссылка={}, id пользователя={}, теги={}, фильтры={}",
            "addTracking",
            link,
            userId,
            tags,
            filters);
        return webClient
            .post()
            .uri(scrapperBaseUrl + "/api/scrapper/track")
            .bodyValue(payload)
            .retrieve()
            .onStatus(
                status -> status.is4xxClientError() || status.is5xxServerError(),
                clientResponse -> clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        log.error("Ошибка HTTP: статус={}, тело={}", clientResponse.statusCode(), errorBody);
                        return Mono.error(new RuntimeException("Ошибка при добавлении отслеживания: " + errorBody));
                    })
            )
            .bodyToMono(Void.class)
            .doOnSuccess(aVoid -> log.info(
                "Запрос на добавление отслеживания выполнен успешно: действие={}, ссылка={}, id пользователя={}",
                "addTracking",
                link,
                userId))
            .doOnError(e -> log.error(
                "Ошибка при добавлении отслеживания: действие={}, ссылка={}, id пользователя={}, ошибка={}",
                "addTracking",
                link,
                userId,
                e.getMessage()));
    }


    public Mono<Void> removeTracking(String link, long userId) {
        UntrackingRequest payload = new UntrackingRequest(link, userId);
        log.info(
            "Отправка запроса на прекращение отслеживания: действие={}, ссылка={}, id пользователя={}",
            "removeTracking",
            link,
            userId);
        return webClient
            .post()
            .uri(scrapperBaseUrl + "/api/scrapper/untrack")
            .bodyValue(payload)
            .retrieve()
            .onStatus(
                status -> status.is4xxClientError() || status.is5xxServerError(),
                clientResponse -> clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        log.error("Ошибка HTTP: статус={}, тело={}", clientResponse.statusCode(), errorBody);
                        return Mono.error(new RuntimeException("Ошибка при прекращении отслеживания: " + errorBody));
                    })
            )
            .bodyToMono(Void.class)
            .doOnSuccess(aVoid -> log.info(
                "Запрос на прекращение отслеживания выполнен успешно: действие={}, ссылка={}, id пользователя={}",
                "removeTracking",
                link,
                userId))
            .doOnError(e -> log.error(
                "Ошибка при прекращении отслеживания: действие={}, ссылка={}, id пользователя={}, ошибка={}",
                "removeTracking",
                link,
                userId,
                e.getMessage()));
    }
}
