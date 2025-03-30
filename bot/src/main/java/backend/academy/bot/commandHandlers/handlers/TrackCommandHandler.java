package backend.academy.bot.commandHandlers.handlers;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.states.TrackCommandState;
import backend.academy.bot.states.TrackStateManager;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TrackCommandHandler implements CommandHandler {

    private final TrackStateManager stateManager;
    private final TelegramBot telegramBot;
    private final ScrapperClient scrapperClient;
    private static final String TRACK_COMMAND = "/track";
    private static final String SEP_REGEX = "\\s+";
    private static final Logger log = LoggerFactory.getLogger(TrackCommandHandler.class);

    public TrackCommandHandler(TrackStateManager stateManager, TelegramBot telegramBot, ScrapperClient scrapperClient) {
        this.stateManager = stateManager;
        this.telegramBot = telegramBot;
        this.scrapperClient = scrapperClient;
    }

    @Override
    public boolean supports(Update update) {
        long userId = Long.parseLong(update.message().from().id().toString());
        return update.message().text().toLowerCase().startsWith(TRACK_COMMAND) || stateManager.getState(userId) != null;
    }

    @Override
    public void handle(Update update) {
        long userId = Long.parseLong(update.message().from().id().toString());
        String text = update.message().text().trim();

        TrackCommandState state = stateManager.getState(userId);

        if (state == null && text.startsWith(TRACK_COMMAND)) {
            String[] parts = text.split(" ");
            if (parts.length < 2) {
                sendMessage(update, "Укажите ссылку, например: /track https://foo.bar/baz");
                return;
            }
            log.info("Получена ссылка {}", parts[1]);
            String link = parts[1];
            stateManager.startTracking(userId, link);
            sendMessage(update, "Введите тэги (опционально)");
        } else if (state != null) {
            if (state.getStep() == TrackCommandState.Step.WAITING_FOR_TAGS) {
                List<String> tags = Arrays.asList(text.split(SEP_REGEX));
                log.info("Получены теги {}", tags);
                Set<String> newTags = new HashSet<>(tags);
                state.setTags(newTags);
                state.nextStep();
                sendMessage(update, "Настройте фильтры (опционально)");
            } else if (state.getStep() == TrackCommandState.Step.WAITING_FOR_FILTERS) {
                List<String> filters = Arrays.asList(text.split(SEP_REGEX));
                Set<String> newFilters = new HashSet<>(filters);
                log.info("Получены фильтры {}", filters);
                state.setFilters(newFilters);
                state.nextStep();
                log.info(
                        "Все данные есть, вот они: ссылка {}, id пользователя {}, теги {}, фильтры {}",
                        state.getLink(),
                        userId,
                        state.getTags(),
                        state.getFilters());
                sendMessage(
                        update,
                        "Ссылка сохранена с тегами: " + state.getTags() + " и фильтрами: " + state.getFilters());
                scrapperClient
                        .addTracking(
                                state.getLink(),
                                userId,
                                new ArrayList<>(state.getTags()),
                                new ArrayList<>(state.getFilters()))
                        .subscribe(
                                unused -> {},
                                error -> telegramBot.execute(new SendMessage(
                                        update.message().chat().id(), "Ошибка передачи данных в скраппер")));
                stateManager.clearState(userId);
            }
        }
    }

    private void sendMessage(Update update, String text) {
        Long chatId = update.message().chat().id();
        SendMessage sendMessage = new SendMessage(chatId, text);
        telegramBot.execute(sendMessage);
    }
}
