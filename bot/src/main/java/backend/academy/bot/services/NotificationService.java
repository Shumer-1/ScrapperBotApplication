package backend.academy.bot.services;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final TelegramBot telegramBot;
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

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
        }
    }
}
