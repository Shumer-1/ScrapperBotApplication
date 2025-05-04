package backend.academy.bot.service.notification;

import backend.academy.avro.NotificationRequest;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerNotificationService implements NotificationService {
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
    public void notify(String message, long userId) {
        SendMessage sendMessage = new SendMessage(userId, message);
        try {
            telegramBot.execute(sendMessage);
            log.info(
                    "Уведомление успешно отправлено: действие={}, сообщение={}, id пользователя={}",
                    "отправлено",
                    message,
                    userId);
        } catch (Exception e) {
            log.error(
                    "Ошибка при отправке уведомления: действие={}, сообщение={}, id пользователя={}, ошибка={}",
                    "отправка",
                    message,
                    userId,
                    e.getMessage());
            throw e;
        }
    }
}
