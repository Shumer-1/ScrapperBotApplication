package backend.academy.scrapper.service.notification;

import backend.academy.scrapper.model.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service(HttpNotificationService.BEAN_NAME)
public class HttpNotificationService implements NotificationService {
    public static final String BEAN_NAME = "httpNotificationService";

    private static final Logger log = LoggerFactory.getLogger(HttpNotificationService.class);
    private final WebClient webClient;

    public HttpNotificationService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Void> sendNotification(String message, long userId) {
        String botNotificationUrl = "http://localhost:8080/api/bot/notify";
        String url = botNotificationUrl + "/api/bot/notify";
        return webClient.post()
            .uri(url)
            .bodyValue(new NotificationRequest(message, userId))
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(v -> log.info("HTTP-уведомление доставлено"))
            .doOnError(error -> log.warn("HTTP-ошибка", error));
    }
}
