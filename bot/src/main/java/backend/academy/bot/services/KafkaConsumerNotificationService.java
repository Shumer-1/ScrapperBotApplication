// src/main/java/backend/academy/bot/services/KafkaConsumerNotificationService.java
package backend.academy.bot.services;

import backend.academy.bot.model.dto.NotificationRequest;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerNotificationService {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerNotificationService.class);
    private final TelegramBot telegramBot;

    public KafkaConsumerNotificationService(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @KafkaListener(topics = "${app.topics.input}", groupId = "bot-group")
    public void listen(NotificationRequest req) {
        if (req.getMessage() == null) {
            throw new IllegalArgumentException("userId and message must not be null");
        }
        log.info("Получено уведомление: message='{}', userId={}", req.getMessage(), req.getUserId());
        telegramBot.execute(new SendMessage(req.getUserId(), req.getMessage()));
        log.info("Уведомление отправлено пользователю {}", req.getUserId());
    }
}
