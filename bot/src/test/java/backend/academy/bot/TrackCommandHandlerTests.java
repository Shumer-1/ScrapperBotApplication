package backend.academy.bot;

import backend.academy.bot.commandHandlers.CommandDispatcher;
import backend.academy.bot.commandHandlers.TrackCommandHandler;
import backend.academy.bot.service.LinkService;
import backend.academy.bot.states.TrackCommandState;
import backend.academy.bot.states.TrackStateManager;
import backend.academy.bot.client.ScrapperClient;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;
import reactor.core.publisher.Mono;
import java.util.List;

public class TrackCommandHandlerTests {

    private LinkService linkService;
    private TrackStateManager stateManager;
    private TelegramBot telegramBot;
    private ScrapperClient scrapperClient;
    private TrackCommandHandler handler;

    @BeforeEach
    public void setUp() throws Exception {
        linkService = mock(LinkService.class);
        stateManager = new TrackStateManager();
        telegramBot = mock(TelegramBot.class);
        scrapperClient = mock(ScrapperClient.class);
        handler = new TrackCommandHandler(linkService, stateManager, telegramBot, scrapperClient);
        when(scrapperClient.addTracking(anyString(), anyLong(), anyList(), anyList())).thenReturn(Mono.empty());
        Field field = CommandDispatcher.class.getDeclaredField("isStarted");
        field.setAccessible(true);
        field.set(null, true);
    }

    @Test
    public void testTrackCommandHandler_HappyPath() {
        Update updateTrack = mock(Update.class);
        Message messageTrack = mock(Message.class);
        Chat chat = mock(Chat.class);
        User user = mock(User.class);
        when(updateTrack.message()).thenReturn(messageTrack);
        when(messageTrack.text()).thenReturn("/track https://github.com/user/repo");
        when(messageTrack.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(12345L);
        when(messageTrack.from()).thenReturn(user);
        when(user.id()).thenReturn(67890L);

        handler.handle(updateTrack);

        TrackCommandState state = stateManager.getState(67890L);
        assertThat(state).isNotNull();
        assertThat(state.getLink()).isEqualTo("https://github.com/user/repo");

        verify(telegramBot, times(1)).execute(argThat((com.pengrad.telegrambot.request.BaseRequest<?, ?> request) -> {
            if (request instanceof SendMessage) {
                SendMessage sm = (SendMessage) request;
                Object text = sm.getParameters().get("text");
                return text != null && text.toString().contains("Введите тэги");
            }
            return false;
        }));

        Update updateTags = mock(Update.class);
        Message messageTags = mock(Message.class);
        Chat chatTags = mock(Chat.class);
        User userTags = mock(User.class);
        when(updateTags.message()).thenReturn(messageTags);
        when(messageTags.text()).thenReturn("java spring");
        when(messageTags.chat()).thenReturn(chatTags);
        when(chatTags.id()).thenReturn(12345L);
        when(messageTags.from()).thenReturn(userTags);
        when(userTags.id()).thenReturn(67890L);

        handler.handle(updateTags);

        state = stateManager.getState(67890L);
        assertThat(state).isNotNull();
        assertThat(state.getTags()).containsExactly("java", "spring");

        verify(telegramBot, atLeastOnce()).execute(argThat((com.pengrad.telegrambot.request.BaseRequest<?, ?> request) -> {
            if (request instanceof SendMessage) {
                SendMessage sm = (SendMessage) request;
                Object text = sm.getParameters().get("text");
                return text != null && text.toString().contains("Настройте фильтры");
            }
            return false;
        }));

        Update updateFilters = mock(Update.class);
        Message messageFilters = mock(Message.class);
        Chat chatFilters = mock(Chat.class);
        User userFilters = mock(User.class);
        when(updateFilters.message()).thenReturn(messageFilters);
        when(messageFilters.text()).thenReturn("filter1 filter2");
        when(messageFilters.chat()).thenReturn(chatFilters);
        when(chatFilters.id()).thenReturn(12345L);
        when(messageFilters.from()).thenReturn(userFilters);
        when(userFilters.id()).thenReturn(67890L);

        handler.handle(updateFilters);

        assertThat(stateManager.getState(67890L)).isNull();

        verify(linkService).addLink(eq("https://github.com/user/repo"), eq(67890L),
            eq(List.of("java", "spring")), eq(List.of("filter1", "filter2")));

        verify(scrapperClient).addTracking(eq("https://github.com/user/repo"), eq(67890L),
            eq(List.of("java", "spring")), eq(List.of("filter1", "filter2")));

        verify(telegramBot, atLeastOnce()).execute(argThat((com.pengrad.telegrambot.request.BaseRequest<?, ?> request) -> {
            if (request instanceof SendMessage) {
                SendMessage sm = (SendMessage) request;
                Object text = sm.getParameters().get("text");
                return text != null && text.toString().contains("Ссылка сохранена");
            }
            return false;
        }));
    }
}
