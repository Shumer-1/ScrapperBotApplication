package backend.academy.scrapper.service.notification;

import backend.academy.scrapper.model.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class HttpNotificationService implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(HttpNotificationService.class);
    private final WebClient webClient;

    public HttpNotificationService(WebClient webClient) {
        this.webClient = webClient;
    }

    public void sendNotification(String message, long userId) {
        log.info("Отправлено уведомление {}", message);
        String botNotificationUrl = "http://localhost:8080/api/bot/notify";
        webClient
                .post()
                .uri(botNotificationUrl)
                .bodyValue(new NotificationRequest(message, userId))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                        unused -> log.info("Уведомление успешно доставлено"),
                        error -> log.error("Ошибка при отправке уведомления", error));
    }
}
