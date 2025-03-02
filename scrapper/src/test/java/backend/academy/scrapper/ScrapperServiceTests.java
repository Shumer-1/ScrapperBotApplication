package backend.academy.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.data.InMemoryTrackingRepository;
import backend.academy.scrapper.data.TrackingRepository;
import backend.academy.scrapper.model.TrackingData;
import backend.academy.scrapper.service.ScrapperService;
import java.time.Instant;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class ScrapperServiceTests {

    private GitHubClient gitHubClient;
    private StackOverflowClient stackOverflowClient;
    private WebClient webClient;
    private TrackingRepository trackingRepository;
    private ScrapperService scrapperService;

    private WebClient.RequestBodyUriSpec postSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    public void setUp() {
        gitHubClient = mock(GitHubClient.class);
        stackOverflowClient = mock(StackOverflowClient.class);
        webClient = mock(WebClient.class);
        trackingRepository = new InMemoryTrackingRepository();

        postSpec = mock(WebClient.RequestBodyUriSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(postSpec).when(webClient).post();
        doReturn(postSpec).when(postSpec).uri(anyString());
        doReturn(postSpec).when(postSpec).bodyValue(any());
        doReturn(responseSpec).when(postSpec).retrieve();
        doReturn(Mono.empty()).when(responseSpec).bodyToMono(Void.class);

        scrapperService = new ScrapperService(gitHubClient, stackOverflowClient, webClient, trackingRepository);
    }

    @Test
    public void testGitHubUpdateNotificationSent() {
        String link = "https://github.com/user/repo";
        long userId = 101;
        TrackingData trackingData =
                new TrackingData(link, userId, new String[] {"tag1"}, new String[] {"filter1"}, null);
        trackingRepository.addTracking(trackingData);

        Instant newUpdate = Instant.parse("2025-03-01T12:00:00Z");
        when(gitHubClient.getLastUpdateTime(link)).thenReturn(Mono.just(newUpdate));

        scrapperService.checkForUpdates();

        Collection<TrackingData> all = trackingRepository.getAllTracking();
        TrackingData td = all.iterator().next();
        assertThat(td.getLastUpdated()).isEqualTo(newUpdate);

        verify(postSpec, atLeastOnce()).uri(argThat((String uriStr) -> uriStr.contains("/api/bot/notify")));
    }

    @Test
    public void testGitHubNoNotificationIfNoNewUpdate() {
        String link = "https://github.com/user/repo";
        long userId = 101;
        Instant previousUpdate = Instant.parse("2025-03-01T12:00:00Z");
        TrackingData trackingData =
                new TrackingData(link, userId, new String[] {"tag1"}, new String[] {"filter1"}, null);
        trackingData.setLastUpdated(previousUpdate);
        trackingRepository.addTracking(trackingData);

        Instant sameOrOlder = Instant.parse("2025-03-01T11:00:00Z");
        when(gitHubClient.getLastUpdateTime(link)).thenReturn(Mono.just(sameOrOlder));

        scrapperService.checkForUpdates();

        Collection<TrackingData> all = trackingRepository.getAllTracking();
        TrackingData td = all.iterator().next();
        assertThat(td.getLastUpdated()).isEqualTo(previousUpdate);

        verify(postSpec, never()).uri(anyString());
    }

    @Test
    public void testStackOverflowUpdateNotificationSent() {
        String link = "https://stackoverflow.com/questions/12345678/sample-question";
        long userId = 202;
        TrackingData trackingData = new TrackingData(link, userId, new String[] {"tag"}, new String[] {"filter"}, null);
        trackingRepository.addTracking(trackingData);

        Instant newActivity = Instant.parse("2025-03-01T15:00:00Z");
        when(stackOverflowClient.getQuestionLastActivity("12345678")).thenReturn(Mono.just(newActivity));

        scrapperService.checkForUpdates();

        Collection<TrackingData> all = trackingRepository.getAllTracking();
        TrackingData td = all.iterator().next();
        assertThat(td.getLastUpdated()).isEqualTo(newActivity);

        verify(postSpec, atLeastOnce()).uri(argThat((String uriStr) -> uriStr.contains("/api/bot/notify")));
    }

    @Test
    public void testStackOverflowNoNotificationIfNoNewUpdate() {
        String link = "https://stackoverflow.com/questions/12345678/sample-question";
        long userId = 202;
        Instant previousActivity = Instant.parse("2025-03-01T15:00:00Z");
        TrackingData trackingData = new TrackingData(link, userId, new String[] {"tag"}, new String[] {"filter"}, null);
        trackingData.setLastUpdated(previousActivity);
        trackingRepository.addTracking(trackingData);

        Instant sameOrOlder = Instant.parse("2025-03-01T14:00:00Z");
        when(stackOverflowClient.getQuestionLastActivity("12345678")).thenReturn(Mono.just(sameOrOlder));

        scrapperService.checkForUpdates();

        Collection<TrackingData> all = trackingRepository.getAllTracking();
        TrackingData td = all.iterator().next();
        assertThat(td.getLastUpdated()).isEqualTo(previousActivity);

        verify(postSpec, never()).uri(anyString());
    }
}
