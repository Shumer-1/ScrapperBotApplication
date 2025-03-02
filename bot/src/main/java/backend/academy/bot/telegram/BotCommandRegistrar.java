package backend.academy.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BotCommandRegistrar {

    private static final Logger log = LoggerFactory.getLogger(BotCommandRegistrar.class);
    private final TelegramBot telegramBot;

    public BotCommandRegistrar(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    public void registerCommands() {
        BotCommand[] commands = new BotCommand[] {
            new BotCommand("/start", "Регистрация пользователя"),
            new BotCommand("/help", "Список доступных команд"),
            new BotCommand("/track", "Начать отслеживание ссылки"),
            new BotCommand("/untrack", "Прекратить отслеживание ссылки"),
            new BotCommand("/list", "Показать отслеживаемые ссылки")
        };

        SetMyCommands setMyCommands = new SetMyCommands(commands);
        try {
            telegramBot.execute(setMyCommands);
            log.info("Команды бота успешно зарегистрированы: команда(ы)={}", (Object) commands);
        } catch (Exception e) {
            log.error("Ошибка при регистрации команд бота: {}", e.getMessage(), e);
        }
    }
}
