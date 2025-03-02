package backend.academy.bot.commandHandlers;

import backend.academy.bot.service.UserService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class StartCommandHandler implements CommandHandler {
    private final UserService userService;
    private final TelegramBot telegramBot;

    public StartCommandHandler(UserService userService, TelegramBot telegramBot) {
        this.userService = userService;
        this.telegramBot = telegramBot;
    }

    @Override
    public boolean supports(Update update) {
        return update.message().text().equalsIgnoreCase("/start");
    }

    @Override
    public void handle(Update update) {
        CommandDispatcher.setIsStarted(true);
        if (update.message() != null && update.message().from() != null) {
            long telegramId = Long.parseLong(update.message().from().id().toString());
            String username = update.message().from().username();
            String firstName = update.message().from().firstName();
            String lastName = update.message().from().lastName();
            if (userService.registerUser(telegramId, username, firstName, lastName)) {
                sendMessage(update, "Пользователь зарегистрирован");
            } else {
                sendMessage(update, "Пользователь уже зарегистрирован");
            }
        }
    }

    private void sendMessage(Update update, String text) {
        Long chatId = update.message().chat().id();
        SendMessage sendMessage = new SendMessage(chatId, text);
        telegramBot.execute(sendMessage);
    }
}
