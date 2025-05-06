package backend.academy.bot.service.notification;

import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Void> notify(String message, long userId);
}

