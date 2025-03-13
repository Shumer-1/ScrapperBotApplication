package backend.academy.bot.commandHandlers;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.service.LinkService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class UntrackCommandHandler implements CommandHandler {
    private final LinkService linkService;
    private final TelegramBot telegramBot;
    private final ScrapperClient scrapperClient;

    public UntrackCommandHandler(LinkService linkService, TelegramBot telegramBot, ScrapperClient scrapperClient) {
        this.linkService = linkService;
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
        if (!CommandDispatcher.isIsStarted()) {
            telegramBot.execute(new SendMessage(update.message().chat().id(), "Сначала нужно зарегистрироваться."));
            return;
        }
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

        boolean deleted = linkService.deleteLinkByUserIdAndLink(userId, link);
        if (deleted) {
            telegramBot.execute(new SendMessage(chatId, "Ссылка успешно удалена из отслеживаемых."));
            scrapperClient
                .removeTracking(link, userId)
                .subscribe(
                    unused -> {
                    },
                    error -> telegramBot.execute(new SendMessage(chatId, "Ошибка удаления данных в скраппер")));
        } else {
            telegramBot.execute(new SendMessage(chatId, "Ссылка не найдена или не отслеживается."));
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId, text);
        telegramBot.execute(sendMessage);
    }
}
