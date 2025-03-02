package backend.academy.bot.commandHandlers;

import backend.academy.bot.service.LinkService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class ListCommandHandler implements CommandHandler {
    private final LinkService linkService;
    private final TelegramBot telegramBot;

    public ListCommandHandler(LinkService linkService, TelegramBot telegramBot) {
        this.linkService = linkService;
        this.telegramBot = telegramBot;
    }

    @Override
    public boolean supports(Update update) {
        return update.message().text().equals("/list");
    }

    @Override
    public void handle(Update update) {
        if (!CommandDispatcher.isIsStarted()) {
            telegramBot.execute(new SendMessage(update.message().chat().id(), "Сначала нужно зарегистрироваться."));
            return;
        }
        long userId = update.message().from().id();
        var links = linkService.getLinksByUserId(userId);

        if (links.isEmpty()) {
            sendMessage(userId, "Пока тут ничего нет");
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < links.size(); i++) {
                stringBuilder.append(i).append(". ").
                    append(links.get(i).link()).append("\n");
            }

            sendMessage(userId, stringBuilder.toString());
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId, text);
        telegramBot.execute(sendMessage);
    }
}
