package backend.academy.bot;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import backend.academy.bot.commandHandlers.CommandDispatcher;
import backend.academy.bot.telegram.TelegramPollingService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TelegramPollingServiceTests {

    private TelegramBot telegramBot;
    private CommandDispatcher commandDispatcher;
    private TelegramPollingService pollingService;

    @BeforeEach
    public void setUp() {
        telegramBot = mock(TelegramBot.class);
        commandDispatcher = new CommandDispatcher(List.of());
        pollingService = new TelegramPollingService(telegramBot, commandDispatcher);
    }

    @Test
    public void testUnknownCommandSendsErrorMessage() throws Exception {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/unknown");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(11111L);

        GetUpdatesResponse response = mock(GetUpdatesResponse.class);
        when(response.updates()).thenReturn(List.of(update));
        when(telegramBot.execute(any(GetUpdates.class))).thenReturn(response);

        Thread pollingThread = new Thread(() -> pollingService.pollUpdates(), "test-polling-thread");
        pollingThread.start();
        Thread.sleep(2000);
        pollingThread.interrupt();
        pollingThread.join();

        verify(telegramBot, atLeastOnce()).execute(argThat((BaseRequest<?, ?> request) -> {
            if (request instanceof SendMessage) {
                SendMessage sm = (SendMessage) request;
                Object text = sm.getParameters().get("text");
                return text != null && text.toString().contains("Такой команды нет!");
            }
            return false;
        }));
    }
}
