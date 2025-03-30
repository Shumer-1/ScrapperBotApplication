package backend.academy.bot.commandHandlers.handlers;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.dto.UntrackingResponse;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UntrackCommandHandler implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(UntrackCommandHandler.class);
    private final TelegramBot telegramBot;
    private final ScrapperClient scrapperClient;

    public UntrackCommandHandler(TelegramBot telegramBot, ScrapperClient scrapperClient) {
        this.telegramBot = telegramBot;
        this.scrapperClient = scrapperClient;
    }

    @Override
    public boolean supports(Update update) {
        String text = update.message().text();
        return text != null && text.trim().toLowerCase().startsWith("/untrack");
    }

    @Override
    public void handle(Update update) {
        String text = update.message().text().trim();
        String[] parts = text.split("\\s+");

        Long chatId = update.message().chat().id();
        if (parts.length < 2) {
            sendMessage(
                    chatId,
                    "Пожалуйста, укажите ссылку, которую необходимо удалить. Пример: /untrack https://foo.bar/baz");
            return;
        }

        String link = parts[1];
        long userId = update.message().from().id();

        scrapperClient
                .removeTracking(link, userId)
                .subscribe(
                        (UntrackingResponse response) -> {
                            if (response.isRemoved()) {
                                sendMessage(chatId, "Ссылка успешно удалена из отслеживаемых.");
                            } else {
                                sendMessage(chatId, "Ссылка не найдена или не отслеживается.");
                            }
                        },
                        error -> {
                            log.error("Ошибка при удалении отслеживания: {}", error.getMessage());
                            sendMessage(chatId, "Ошибка удаления данных в скраппер: " + error.getMessage());
                        });
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId, text);
        telegramBot.execute(sendMessage);
    }
}
