package backend.academy.bot.service.notification;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
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
    private final CircuitBreaker notificationCb;
    private static final Logger log = LoggerFactory.getLogger(FallbackNotificationService.class);

    public FallbackNotificationService(
        @Qualifier(HttpNotificationService.BEAN_NAME) NotificationService http,
        @Qualifier(KafkaConsumerNotificationService.BEAN_NAME) NotificationService kafka,
        @Value("${app.message-transport:http}") String primaryTransport,
        @Qualifier("notificationCb") CircuitBreaker notificationCb) {
        this.notificationCb = notificationCb;
        if ("kafka".equalsIgnoreCase(primaryTransport)) {
            this.primary = kafka;
            this.secondary = http;
        } else {
            this.primary = http;
            this.secondary = kafka;
        }
    }

    @Override
    public Mono<Void> notify(String msg, long user) {
        return primary.notify(msg, user)
            .transformDeferred(CircuitBreakerOperator.of(notificationCb))
            .onErrorResume(ex -> {
                log.warn("Первичный транспорт упал - fallback. Причина: {}", ex.getMessage());
                return secondary.notify(msg, user);
            });
    }
}

