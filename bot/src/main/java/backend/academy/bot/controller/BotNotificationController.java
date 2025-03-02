package backend.academy.bot.controller;

import backend.academy.bot.model.NotificationRequest;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bot")
public class BotNotificationController {
    private static final Logger log = LoggerFactory.getLogger(BotNotificationController.class);

    private final TelegramBot telegramBot;

    public BotNotificationController(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostMapping("/notify")
    public void receiveNotification(@RequestBody NotificationRequest request) {
        log.info(
                "Получено уведомление от скраппера: действие={}, сообщение={}, id пользователя={}",
                "получено",
                request.getMessage(),
                request.getUserId());
        String messageText = request.getMessage();
        SendMessage sendMessage = new SendMessage(request.getUserId(), messageText);
        try {
            telegramBot.execute(sendMessage);
            log.info(
                    "Уведомление успешно отправлено: действие={}, сообщение={}, id пользователя={}",
                    "отправлено",
                    request.getMessage(),
                    request.getUserId());
        } catch (Exception e) {
            log.error(
                    "Ошибка при отправке уведомления: действие={}, сообщение={}, id пользователя={}, ошибка={}",
                    "отправка",
                    request.getMessage(),
                    request.getUserId(),
                    e.getMessage());
        }
    }
}
