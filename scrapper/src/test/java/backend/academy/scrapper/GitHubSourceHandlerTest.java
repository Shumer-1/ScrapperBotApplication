package backend.academy.scrapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.model.dto.GitHubIssue;
import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.model.entities.User;
import backend.academy.scrapper.service.linkService.LinkService;
import backend.academy.scrapper.service.notification.NotificationService;
import backend.academy.scrapper.service.sourceHandlers.GitHubSourceHandler;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class GitHubSourceHandlerTest {

    private GitHubClient gitHubClient;
    private LinkService linkService;
    private NotificationService notificationService;
    private GitHubSourceHandler sourceHandler;

    @BeforeEach
    public void setup() {
        gitHubClient = Mockito.mock(GitHubClient.class);
        linkService = Mockito.mock(LinkService.class);
        notificationService = Mockito.mock(NotificationService.class);
        sourceHandler = new GitHubSourceHandler(gitHubClient, notificationService, linkService);
    }

    @Test
    public void testCanHandle() {
        String githubLink = "https://github.com/user/repo/issues/123";
        String nonGithubLink = "https://stackoverflow.com/questions/12345";
        assert (sourceHandler.canHandle(githubLink));
        assert (!sourceHandler.canHandle(nonGithubLink));
    }

    @Test
    public void testProcessWhenIssueUpdateNewer() {
        User user = new User();
        user.setTelegramId(400L);
        Link link = new Link();
        link.setLink("https://github.com/user/repo/issues/123");
        link.setUser(user);
        link.setLastUpdated(Instant.now().minus(1, ChronoUnit.DAYS));

        Instant updateTime = Instant.now();
        GitHubIssue issueUpdate = new GitHubIssue("Issue Title", "GitHubUser", updateTime, "Issue preview");
        when(gitHubClient.getLatestIssueOrPR(link.getLink())).thenReturn(Mono.just(issueUpdate));

        Mono<Void> result = sourceHandler.process(link);

        StepVerifier.create(result).expectComplete().verify();

        verify(notificationService, times(1)).sendNotification(contains("Issue Title"), eq(400L));
        verify(linkService, times(1)).refreshLastUpdated(eq(link.getLink()), eq(400L), eq(updateTime));
    }

    @Test
    public void testProcessWhenNoUpdate() {
        User user = new User();
        user.setTelegramId(500L);
        Link link = new Link();
        link.setLink("https://github.com/user/repo/issues/456");
        link.setUser(user);
        link.setLastUpdated(Instant.now());

        when(gitHubClient.getLatestIssueOrPR(link.getLink())).thenReturn(Mono.empty());

        Mono<Void> result = sourceHandler.process(link);

        StepVerifier.create(result).expectComplete().verify();

        verify(notificationService, never()).sendNotification(anyString(), anyLong());
        verify(linkService, never()).refreshLastUpdated(anyString(), anyLong(), any());
    }
}
