package backend.academy.bot.commandHandlers.handlers;

import backend.academy.bot.client.ScrapperClient;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class StartCommandHandler implements CommandHandler {
    private final ScrapperClient scrapperClient;
    private final TelegramBot telegramBot;

    public StartCommandHandler(ScrapperClient scrapperClient, TelegramBot telegramBot) {
        this.scrapperClient = scrapperClient;
        this.telegramBot = telegramBot;
    }

    @Override
    public boolean supports(Update update) {
        return update.message().text().equalsIgnoreCase("/start");
    }

    @Override
    public void handle(Update update) {
        if (update.message() != null && update.message().from() != null) {
            long telegramId = Long.parseLong(update.message().from().id().toString());
            String username = update.message().from().username();
            scrapperClient
                    .registerUser(telegramId, username)
                    .subscribe(
                            response -> {
                                if (response.isExists()) {
                                    sendMessage(update, "Пользователь уже зарегистрирован");
                                } else {
                                    sendMessage(update, "Пользователь зарегистрирован");
                                }
                            },
                            error -> {
                                sendMessage(update, "Ошибка при регистрации: " + error.getMessage());
                            });
        }
    }

    private void sendMessage(Update update, String text) {
        Long chatId = update.message().chat().id();
        SendMessage sendMessage = new SendMessage(chatId, text);
        telegramBot.execute(sendMessage);
    }
}
