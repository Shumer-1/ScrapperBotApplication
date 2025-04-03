package backend.academy.bot.commandHandlers.handlers;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.entities.Link;
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
    private final ScrapperClient scrapperClient;

    public ListCommandHandler(ScrapperClient scrapperClient, TelegramBot telegramBot) {
        this.scrapperClient = scrapperClient;
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
        log.info("Обрабатываем команду /list от {}", update.message().from().id());
        long telegramId = Long.parseLong(update.message().from().id().toString());
        scrapperClient
                .getLinksByUserId(telegramId)
                .subscribe(
                        links -> {
                            if (links.isEmpty()) {
                                log.info(
                                        "Ответ на /list: пусто для {}",
                                        update.message().from().id());
                                sendMessage(update, "Пока тут ничего нет");
                            } else {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < links.size(); i++) {
                                    Link link = links.get(i);
                                    sb.append(i)
                                            .append(". ")
                                            .append(link.getLink())
                                            .append("\n");
                                }
                                log.info(
                                        "Ответ на /list: есть данные для {}:{}",
                                        update.message().from().id(),
                                        sb);
                                sendMessage(update, sb.toString());
                            }
                        },
                        error -> {
                            log.error(
                                    "Ошибка при получении ссылок для пользователя {}: {}",
                                    telegramId,
                                    error.getMessage());
                            sendMessage(update, "Ошибка при получении списка ссылок: " + error.getMessage());
                        });
    }

    private void sendMessage(Update update, String text) {
        Long chatId = update.message().chat().id();
        SendMessage sendMessage = new SendMessage(chatId, text);
        telegramBot.execute(sendMessage);
    }
}
