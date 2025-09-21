package backend.academy.bot.commandHandlers.handlers;

import backend.academy.bot.service.LinkCacheService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ListCommandHandler implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(ListCommandHandler.class);
    private final TelegramBot telegramBot;
    private final LinkCacheService linkCacheService;

    public ListCommandHandler(LinkCacheService linkCacheService, TelegramBot telegramBot) {
        this.linkCacheService = linkCacheService;
        this.telegramBot = telegramBot;
    }

    @Override
    public boolean supports(Update update) {
        return update.message() != null
                && "/list".equalsIgnoreCase(update.message().text());
    }

    @Override
    public void handle(Update update) {
        if (update.message() == null || update.message().from() == null) {
            return;
        }
        long telegramId = update.message().from().id();
        log.info("Обрабатываем команду /list от {}", telegramId);

        linkCacheService
                .getLinks(telegramId)
                .subscribe(
                        links -> {
                            if (links.isEmpty()) {
                                sendMessage(update, "Пока тут ничего нет");
                            } else {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < links.size(); i++) {
                                    sb.append(i)
                                            .append(". ")
                                            .append(links.get(i).getLink())
                                            .append("\n");
                                }
                                sendMessage(update, sb.toString());
                            }
                        },
                        error -> {
                            log.error("Ошибка при получении ссылок для {}: {}", telegramId, error.getMessage());
                            sendMessage(update, "Ошибка при получении списка ссылок: " + error.getMessage());
                        });
    }

    private void sendMessage(Update update, String text) {
        telegramBot.execute(new SendMessage(update.message().chat().id(), text));
    }
}
