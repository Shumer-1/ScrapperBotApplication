package backend.academy.bot.commandHandlers;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.service.LinkService;
import backend.academy.bot.states.TrackCommandState;
import backend.academy.bot.states.TrackStateManager;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TrackCommandHandler implements CommandHandler {

    private final LinkService linkService;
    private final TrackStateManager stateManager;
    private final TelegramBot telegramBot;
    private final ScrapperClient scrapperClient;
    private static final String TRACK_COMMAND = "/track";
    private static final String SEP_REGEX = "\\s+";

    public TrackCommandHandler(
        LinkService linkService,
        TrackStateManager stateManager,
        TelegramBot telegramBot,
        ScrapperClient scrapperClient) {
        this.linkService = linkService;
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
        if (!CommandDispatcher.isIsStarted()) {
            telegramBot.execute(new SendMessage(update.message().chat().id(), "Сначала нужно зарегистрироваться."));
            return;
        }
        long userId = Long.parseLong(update.message().from().id().toString());
        String text = update.message().text().trim();

        TrackCommandState state = stateManager.getState(userId);

        if (state == null && text.startsWith(TRACK_COMMAND)) {
            String[] parts = text.split(" ");
            if (parts.length < 2) {
                sendMessage(update, "Укажите ссылку, например: /track https://foo.bar/baz");
                return;
            }
            String link = parts[1];
            stateManager.startTracking(userId, link);
            sendMessage(update, "Введите тэги (опционально)");
        } else if (state != null) {
            if (state.getStep() == TrackCommandState.Step.WAITING_FOR_TAGS) {
                List<String> tags = Arrays.asList(text.split(SEP_REGEX));
                state.setTags(tags);
                state.nextStep();
                sendMessage(update, "Настройте фильтры (опционально)");
            } else if (state.getStep() == TrackCommandState.Step.WAITING_FOR_FILTERS) {
                List<String> filters = Arrays.asList(text.split(SEP_REGEX));
                state.setFilters(filters);
                state.nextStep();
                linkService.addLink(state.getLink(), userId, state.getTags(), state.getFilters());
                sendMessage(
                    update,
                    "Ссылка сохранена с тегами: " + state.getTags() + " и фильтрами: " + state.getFilters());
                scrapperClient
                    .addTracking(state.getLink(), userId, state.getTags(), state.getFilters())
                    .subscribe(
                        unused -> {
                        },
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
