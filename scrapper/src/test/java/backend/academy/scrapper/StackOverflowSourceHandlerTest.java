package backend.academy.scrapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.model.dto.StackOverflowUpdate;
import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.model.entities.User;
import backend.academy.scrapper.service.linkService.LinkService;
import backend.academy.scrapper.service.notification.NotificationService;
import backend.academy.scrapper.service.sourceHandlers.StackOverflowSourceHandler;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class StackOverflowSourceHandlerTest {

    private StackOverflowClient stackOverflowClient;
    private LinkService linkService;
    private NotificationService notificationService;
    private StackOverflowSourceHandler sourceHandler;

    @BeforeEach
    public void setup() {
        stackOverflowClient = Mockito.mock(StackOverflowClient.class);
        linkService = Mockito.mock(LinkService.class);
        notificationService = Mockito.mock(NotificationService.class);
        sourceHandler = new StackOverflowSourceHandler(stackOverflowClient, linkService, notificationService);
    }

    @Test
    public void testCanHandle() {
        String soLink = "https://stackoverflow.com/questions/12345/example";
        String otherLink = "https://github.com/user/repo";
        // canHandle должен вернуть true для ссылок StackOverflow
        assert (sourceHandler.canHandle(soLink));
        // и false для остальных
        assert (!sourceHandler.canHandle(otherLink));
    }

    @Test
    public void testProcessWhenBothAnswerAndCommentExist_AnswerNewer() {
        // Подготавливаем объект Link
        User user = new User();
        user.setTelegramId(100L);
        Link link = new Link();
        link.setLink("https://stackoverflow.com/questions/54321/example");
        link.setUser(user);
        link.setLastUpdated(null);

        String questionId = "54321";

        // Мокаем получение обновлений: и ответ, и комментарий
        Instant answerTime = Instant.now();
        Instant commentTime = answerTime.minusSeconds(60);
        StackOverflowUpdate answer = new StackOverflowUpdate("Question Title", "AuthorA", answerTime, "Answer preview");
        StackOverflowUpdate comment =
                new StackOverflowUpdate("Question Title", "AuthorB", commentTime, "Comment preview");

        when(stackOverflowClient.getLatestAnswer(questionId)).thenReturn(Mono.just(answer));
        when(stackOverflowClient.getLatestComment(questionId)).thenReturn(Mono.just(comment));

        Mono<Void> result = sourceHandler.process(link);

        StepVerifier.create(result).expectComplete().verify();

        // Ожидаем, что выберется ответ (так как он новее)
        verify(notificationService, times(1)).sendNotification(contains("Question Title"), eq(100L));
        verify(linkService, times(1)).refreshLastUpdated(eq(link.getLink()), eq(100L), eq(answerTime));
    }

    @Test
    public void testProcessWhenOnlyCommentExists() {
        // Если один из Mono пустой, zip возвращает пустой Mono
        // Поэтому уведомление отправляться не должно.
        User user = new User();
        user.setTelegramId(200L);
        Link link = new Link();
        link.setLink("https://stackoverflow.com/questions/54321/another");
        link.setUser(user);
        link.setLastUpdated(null);

        String questionId = "54321";
        Instant commentTime = Instant.now();

        when(stackOverflowClient.getLatestAnswer(questionId)).thenReturn(Mono.empty());
        StackOverflowUpdate comment =
                new StackOverflowUpdate("Another Question", "AuthorC", commentTime, "Comment preview");
        when(stackOverflowClient.getLatestComment(questionId)).thenReturn(Mono.just(comment));

        Mono<Void> result = sourceHandler.process(link);

        StepVerifier.create(result).expectComplete().verify();

        verify(notificationService, never()).sendNotification(anyString(), anyLong());
        verify(linkService, never()).refreshLastUpdated(anyString(), anyLong(), any());
    }

    @Test
    public void testProcessWhenNoUpdatesExist() {
        // Если и ответ и комментарий отсутствуют, никаких уведомлений не отправляем
        User user = new User();
        user.setTelegramId(300L);
        Link link = new Link();
        link.setLink("https://stackoverflow.com/questions/54321/none");
        link.setUser(user);
        link.setLastUpdated(null);

        String questionId = "54321";

        when(stackOverflowClient.getLatestAnswer(questionId)).thenReturn(Mono.empty());
        when(stackOverflowClient.getLatestComment(questionId)).thenReturn(Mono.empty());

        Mono<Void> result = sourceHandler.process(link);

        StepVerifier.create(result).expectComplete().verify();

        verify(notificationService, never()).sendNotification(anyString(), anyLong());
        verify(linkService, never()).refreshLastUpdated(anyString(), anyLong(), any());
    }
}
