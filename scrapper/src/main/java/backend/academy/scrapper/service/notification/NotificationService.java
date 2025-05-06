package backend.academy.scrapper.service.notification;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public interface NotificationService {
    Mono<Void> sendNotification(String message, long userId);
}
