package backend.academy.bot.commandHandlers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class HelpCommandHandler implements CommandHandler {
    private final TelegramBot telegramBot;

    public HelpCommandHandler(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public boolean supports(Update update) {
        return update.message().text().equalsIgnoreCase("/help");
    }

    @Override
    public void handle(Update update) {
        if (!CommandDispatcher.isIsStarted()) {
            telegramBot.execute(new SendMessage(update.message().chat().id(), "Сначала нужно зарегистрироваться."));
            return;
        }
        String responseText =
            """
                /start - регистрация пользователя.\n
                /help - вывод списка доступных команд.\n
                /track <ссылка> - начать отслеживание ссылки.\n
                /untrack <ссылка> - прекратить отслеживание ссылки.\n
                /list - показать список отслеживаемых ссылок (cписок ссылок, полученных при /track)
                """;
        SendMessage sendMessage = new SendMessage(update.message().chat().id(), responseText);
        telegramBot.execute(sendMessage);
    }
}
