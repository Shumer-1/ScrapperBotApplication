package backend.academy.bot.commandHandlers.handlers;

import com.pengrad.telegrambot.model.Update;

public interface CommandHandler {
    boolean supports(Update update);

    void handle(Update update);
}
