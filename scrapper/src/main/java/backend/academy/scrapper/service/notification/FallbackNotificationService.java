package backend.academy.scrapper.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Primary
public class FallbackNotificationService implements NotificationService {

    private final NotificationService primary;
    private final NotificationService secondary;
    private static final Logger log = LoggerFactory.getLogger(FallbackNotificationService.class);

    public FallbackNotificationService(
        @Qualifier("httpNotificationService")  NotificationService http,
        @Qualifier("kafkaNotificationService") NotificationService kafka,
        @Value("${app.message-transport:http}") String primaryTransport) {

        if ("kafka".equalsIgnoreCase(primaryTransport)) {
            this.primary   = kafka;
            this.secondary = http;
        } else {
            this.primary   = http;
            this.secondary = kafka;
        }
    }

    @Override
    public Mono<Void> sendNotification(String message, long userId) {
        return primary.sendNotification(message, userId)
            .onErrorResume(exception -> {
                log.warn("Первичный транспорт упал - fallback. Причина: {}", exception.getMessage());
                return secondary.sendNotification(message, userId);
            });
    }
}

