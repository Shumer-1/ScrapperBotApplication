package backend.academy.bot.service.notification;

import backend.academy.avro.NotificationRequest;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service(KafkaConsumerNotificationService.BEAN_NAME)
@ConditionalOnProperty(name = "app.message-transport", havingValue = "Kafka")
public class KafkaConsumerNotificationService implements NotificationService {
    public static final String BEAN_NAME = "kafkaNotificationService";

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerNotificationService.class);
    private final TelegramBot telegramBot;

    public KafkaConsumerNotificationService(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @KafkaListener(
            topics = "${app.topics.input}",
            groupId = "bot-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void listen(List<NotificationRequest> batch, Acknowledgment ack) {
        for (var req : batch) {
            process(req);
        }
        ack.acknowledge();
    }

    private void process(NotificationRequest req) {
        if (req.getMessage() == null) {
            throw new IllegalArgumentException("Message must not be null");
        }

        notify(req.getMessage(), req.getUserId());
    }

    @Override
    public Mono<Void> notify(String message, long userId) {
        return Mono.fromCallable(() -> {
                telegramBot.execute(new SendMessage(userId, message));
                return (Void) null;
            })
            .doOnSuccess(v -> log.info("Kafka-уведомление доставлено: {}", message))
            .doOnError(error -> log.error("Ошибка Kafka-уведомления", error))
            .subscribeOn(Schedulers.boundedElastic());
    }
}
