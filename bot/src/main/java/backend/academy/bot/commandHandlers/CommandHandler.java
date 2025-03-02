package backend.academy.bot.commandHandlers;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public interface CommandHandler {
    boolean supports(Update update);

    void handle(Update update);
}
