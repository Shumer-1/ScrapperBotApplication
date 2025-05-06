package backend.academy.bot.service.notification;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service("httpNotificationService")
public class HttpNotificationService implements NotificationService {
    private final TelegramBot telegramBot;
    private static final Logger log = LoggerFactory.getLogger(HttpNotificationService.class);

    public HttpNotificationService(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public Mono<Void> notify(String message, long userId) {
        return Mono.fromCallable(() -> {
                telegramBot.execute(new SendMessage(userId, message));
                return (Void) null;
            })
            .doOnSuccess(v -> log.info("HTTP-уведомление доставлено: {}", message))
            .doOnError(e -> log.error("Ошибка HTTP-уведомления", e))
            .subscribeOn(Schedulers.boundedElastic());
    }
}
