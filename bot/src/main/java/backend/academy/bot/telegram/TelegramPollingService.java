package backend.academy.bot.telegram;

import backend.academy.bot.commandHandlers.CommandDispatcher;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TelegramPollingService {

    private static final Logger log = LoggerFactory.getLogger(TelegramPollingService.class);
    private final TelegramBot telegramBot;
    private final CommandDispatcher commandDispatcher;
    private int lastUpdateId = 0;
    private final int limit = 100;
    private final int timeout = 10;

    public TelegramPollingService(TelegramBot telegramBot, CommandDispatcher commandDispatcher) {
        this.telegramBot = telegramBot;
        this.commandDispatcher = commandDispatcher;
    }

    @PostConstruct
    public void startPolling() {
        Thread pollingThread = new Thread(this::pollUpdates, "polling-thread");
        pollingThread.start();
        log.info("Запущен поток опроса Telegram: имя_потока={}", pollingThread.getName());
    }

    public void pollUpdates() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                GetUpdates getUpdates = new GetUpdates()
                    .limit(limit)
                    .offset(lastUpdateId + 1)
                    .timeout(timeout);
                log.info("Отправляем запрос обновлений: limit={}, offset={}, timeout={}",
                    limit, lastUpdateId + 1, timeout);
                GetUpdatesResponse updatesResponse = telegramBot.execute(getUpdates);
                List<Update> updates = updatesResponse.updates();

                if (updates != null && !updates.isEmpty()) {
                    log.info("Получено обновлений: количество={}", updates.size());
                    for (Update update : updates) {
                        lastUpdateId = update.updateId();
                        log.info("Обрабатываем обновление: updateId={}", update.updateId());
                        if (!commandDispatcher.dispatch(update)) {
                            long chatId = update.message().chat().id();
                            String text = "Такой команды нет!";
                            SendMessage sendMessage = new SendMessage(chatId, text);
                            telegramBot.execute(sendMessage);
                            log.info("Отправлено сообщение о неизвестной команде: chatId={}, текст={}",
                                chatId, text);
                        }
                    }
                } else {
                    log.debug("Новых обновлений не получено.");
                }
            } catch (Exception e) {
                log.error("Ошибка при выполнении запроса обновлений: ошибка={}", e.getMessage(), e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    log.error("Поток сна прерван: ошибка={}", ie.getMessage(), ie);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
