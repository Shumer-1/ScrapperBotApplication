package backend.academy.bot.commandHandlers;

import com.pengrad.telegrambot.model.Update;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CommandDispatcher {
    private final List<CommandHandler> handlers;
    private static boolean isStarted = false;

    public CommandDispatcher(List<CommandHandler> handlers) {
        this.handlers = handlers;
    }

    public static boolean isIsStarted() {
        return isStarted;
    }

    public static void setIsStarted(boolean isStarted) {
        CommandDispatcher.isStarted = isStarted;
    }

    public boolean dispatch(Update update) {
        for (var handler : handlers) {
            if (handler.supports(update)) {
                handler.handle(update);
                return true;
            }
        }
        return false;
    }
}
