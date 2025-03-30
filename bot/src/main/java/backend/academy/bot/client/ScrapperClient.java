package backend.academy.bot.client;

import backend.academy.bot.model.dto.TrackingRequest;
import backend.academy.bot.model.dto.UntrackingRequest;
import backend.academy.bot.model.dto.UntrackingResponse;
import backend.academy.bot.model.dto.UserRegistrationRequest;
import backend.academy.bot.model.dto.UserRegistrationResponse;
import backend.academy.bot.model.entities.Link;
import backend.academy.bot.services.NotificationService;
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
    private final NotificationService notificationService;
    private final String scrapperBaseUrl;

    public ScrapperClient(
            WebClient webClient,
            NotificationService notificationService,
            @Value("${scrapper.base-url}") String scrapperBaseUrl) {
        this.webClient = webClient;
        this.notificationService = notificationService;
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
                        clientResponse -> clientResponse
                                .bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error(
                                            "Ошибка HTTP: статус={}, тело={}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(
                                            new RuntimeException("Ошибка при добавлении отслеживания: " + errorBody));
                                }))
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

    public Mono<UntrackingResponse> removeTracking(String link, long userId) {
        UntrackingRequest payload = new UntrackingRequest(link, userId);
        log.info(
                "Отправка запроса на прекращение отслеживания: действие=removeTracking, ссылка={}, id пользователя={}",
                link,
                userId);
        return webClient
                .post()
                .uri(scrapperBaseUrl + "/api/scrapper/untrack")
                .bodyValue(payload)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse
                                .bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error(
                                            "Ошибка HTTP: статус={}, тело={}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(
                                            new RuntimeException("Ошибка при прекращении отслеживания: " + errorBody));
                                }))
                .bodyToMono(UntrackingResponse.class)
                .doOnSuccess(response -> log.info("Ответ от скраппера: {}", response.getMessage()))
                .doOnError(e -> log.error(
                        "Ошибка при прекращении отслеживания: действие=removeTracking, ссылка={}, id пользователя={}, ошибка={}",
                        link,
                        userId,
                        e.getMessage()));
    }

    public Mono<UserRegistrationResponse> registerUser(long userId, String username) {
        UserRegistrationRequest payload = new UserRegistrationRequest(userId, username);
        log.info("Отправка запроса на регистрацию пользователя: id={}, username={}", userId, username);
        return webClient
                .post()
                .uri(scrapperBaseUrl + "/api/scrapper/user")
                .bodyValue(payload)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse
                                .bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error(
                                            "Ошибка HTTP: статус={}, тело={}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(
                                            new RuntimeException("Ошибка при регистрации пользователя: " + errorBody));
                                }))
                .bodyToMono(UserRegistrationResponse.class)
                .doOnSuccess(response -> {
                    log.info("Ответ от скраппера: {}", response.getMessage());
                })
                .doOnError(e -> log.error(
                        "Ошибка при регистрации пользователя: id={}, username={}, ошибка={}",
                        userId,
                        username,
                        e.getMessage()));
    }

    public Mono<List<Link>> getLinksByUserId(long userId) {
        return webClient
                .get()
                .uri(scrapperBaseUrl + "/api/scrapper/links?userId=" + userId)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse
                                .bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error(
                                            "Ошибка HTTP при получении ссылок: статус={}, тело={}",
                                            clientResponse.statusCode(),
                                            errorBody);
                                    return Mono.error(
                                            new RuntimeException("Ошибка при получении ссылок: " + errorBody));
                                }))
                .bodyToFlux(Link.class)
                .collectList()
                .doOnSuccess(links -> log.info("Получено {} ссылок для пользователя {}", links.size(), userId))
                .doOnError(
                        e -> log.error("Ошибка при получении ссылок для пользователя {}: {}", userId, e.getMessage()));
    }
}
