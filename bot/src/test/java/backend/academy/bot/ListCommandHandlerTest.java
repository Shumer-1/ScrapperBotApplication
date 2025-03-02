package backend.academy.bot;

import backend.academy.bot.commandHandlers.CommandDispatcher;
import backend.academy.bot.commandHandlers.ListCommandHandler;
import backend.academy.bot.model.Link;
import backend.academy.bot.service.LinkService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListCommandHandlerTest {

    private LinkService linkService;
    private TelegramBot telegramBot;
    private ListCommandHandler handler;

    @BeforeEach
    public void setUp() throws Exception {
        linkService = mock(LinkService.class);
        telegramBot = mock(TelegramBot.class);
        handler = new ListCommandHandler(linkService, telegramBot);

        var field = CommandDispatcher.class.getDeclaredField("isStarted");
        field.setAccessible(true);
        field.set(null, true);
    }

    @Test
    public void testListCommandFormatting_WhenLinksPresent() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        User user = mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/list");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(55555L);
        when(message.from()).thenReturn(user);
        when(user.id()).thenReturn(99999L);

        Link link1 = new Link("https://github.com/user/repo1", 99999L, 1, List.of("tag1"), List.of("filter1"));
        Link link2 = new Link("https://github.com/user/repo2", 99999L, 2, List.of("tag2"), List.of("filter2"));
        when(linkService.getLinksByUserId(99999L)).thenReturn(List.of(link1, link2));

        handler.handle(update);

        String expectedText = "0. https://github.com/user/repo1\n1. https://github.com/user/repo2\n";

        verify(telegramBot).execute(argThat((BaseRequest<?, ?> request) -> {
            if (request instanceof SendMessage) {
                SendMessage sm = (SendMessage) request;
                Object text = sm.getParameters().get("text");
                return expectedText.equals(text);
            }
            return false;
        }));
    }

    @Test
    public void testListCommandFormatting_WhenNoLinks() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        User user = mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/list");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(55555L);
        when(message.from()).thenReturn(user);
        when(user.id()).thenReturn(99999L);

        when(linkService.getLinksByUserId(99999L)).thenReturn(List.of());

        handler.handle(update);

        verify(telegramBot).execute(argThat((BaseRequest<?, ?> request) -> {
            if (request instanceof SendMessage) {
                SendMessage sm = (SendMessage) request;
                Object text = sm.getParameters().get("text");
                return text != null && text.toString().contains("Пока тут ничего нет");
            }
            return false;
        }));
    }
}
