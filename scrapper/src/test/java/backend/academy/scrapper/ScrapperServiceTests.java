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

import backend.academy.scrapper.data.InMemoryTrackingRepository;
import backend.academy.scrapper.data.TrackingRepository;
import backend.academy.scrapper.model.TrackingData;
import backend.academy.scrapper.service.GitHubSourceHandler;
import backend.academy.scrapper.service.ScrapperService;
import backend.academy.scrapper.service.StackOverflowSourceHandler;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class ScrapperServiceTests {

    private GitHubSourceHandler gitHubSourceHandler;
    private StackOverflowSourceHandler stackOverflowSourceHandler;

    private WebClient webClient;
    private TrackingRepository trackingRepository;
    private ScrapperService scrapperService;

    private WebClient.RequestBodyUriSpec postSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    public void setUp() {
        gitHubSourceHandler = mock(GitHubSourceHandler.class);
        stackOverflowSourceHandler = mock(StackOverflowSourceHandler.class);
        webClient = mock(WebClient.class);
        trackingRepository = new InMemoryTrackingRepository();

        postSpec = mock(WebClient.RequestBodyUriSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(postSpec).when(webClient).post();
        doReturn(postSpec).when(postSpec).uri(anyString());
        doReturn(postSpec).when(postSpec).bodyValue(any());
        doReturn(responseSpec).when(postSpec).retrieve();
        doReturn(Mono.empty()).when(responseSpec).bodyToMono(Void.class);

        scrapperService =
                new ScrapperService(List.of(gitHubSourceHandler, stackOverflowSourceHandler), trackingRepository);
    }

    @Test
    public void testGitHubUpdateNotificationSent() throws InterruptedException {
        String link = "https://github.com/user/repo";
        long userId = 101;
        TrackingData trackingData =
                new TrackingData(link, userId, new String[] {"tag1"}, new String[] {"filter1"}, null);
        trackingRepository.addTracking(trackingData);

        Instant newUpdate = Instant.parse("2025-03-01T12:00:00Z");

        when(gitHubSourceHandler.canHandle(link)).thenReturn(true);
        when(gitHubSourceHandler.process(any())).thenAnswer(invocation -> {
            TrackingData td = invocation.getArgument(0);
            td.setLastUpdated(newUpdate);
            webClient
                    .post()
                    .uri("/api/bot/notify")
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe();
            return Mono.just(td);
        });

        when(stackOverflowSourceHandler.canHandle(link)).thenReturn(false);

        scrapperService.checkForUpdates();
        Thread.sleep(200);
        Collection<TrackingData> all = trackingRepository.getAllTracking();
        TrackingData td = all.iterator().next();
        assertThat(td.getLastUpdated()).isEqualTo(newUpdate);

        verify(postSpec, atLeastOnce()).uri(argThat((String uriStr) -> uriStr.contains("/api/bot/notify")));
    }

    @Test
    public void testGitHubNoNotificationIfNoNewUpdate() throws InterruptedException {
        String link = "https://github.com/user/repo";
        long userId = 101;
        Instant previousUpdate = Instant.parse("2025-03-01T12:00:00Z");
        TrackingData trackingData =
                new TrackingData(link, userId, new String[] {"tag1"}, new String[] {"filter1"}, null);
        trackingData.setLastUpdated(previousUpdate);
        trackingRepository.addTracking(trackingData);

        when(gitHubSourceHandler.canHandle(link)).thenReturn(true);
        when(gitHubSourceHandler.process(any())).thenReturn(Mono.empty());

        when(stackOverflowSourceHandler.canHandle(link)).thenReturn(false);

        scrapperService.checkForUpdates();

        Thread.sleep(200);

        Collection<TrackingData> all = trackingRepository.getAllTracking();
        TrackingData td = all.iterator().next();
        assertThat(td.getLastUpdated()).isEqualTo(previousUpdate);

        verify(postSpec, never()).uri(anyString());
    }

    @Test
    public void testStackOverflowUpdateNotificationSent() throws InterruptedException {
        String link = "https://stackoverflow.com/questions/12345678/sample-question";
        long userId = 202;
        TrackingData trackingData = new TrackingData(link, userId, new String[] {"tag"}, new String[] {"filter"}, null);
        trackingRepository.addTracking(trackingData);

        Instant newActivity = Instant.parse("2025-03-01T15:00:00Z");

        when(stackOverflowSourceHandler.canHandle(link)).thenReturn(true);
        when(stackOverflowSourceHandler.process(any())).thenAnswer(invocation -> {
            TrackingData td = invocation.getArgument(0);
            td.setLastUpdated(newActivity);
            webClient
                    .post()
                    .uri("/api/bot/notify")
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe();
            return Mono.just(td);
        });

        when(gitHubSourceHandler.canHandle(link)).thenReturn(false);

        scrapperService.checkForUpdates();

        Thread.sleep(200);

        Collection<TrackingData> all = trackingRepository.getAllTracking();
        TrackingData td = all.iterator().next();
        assertThat(td.getLastUpdated()).isEqualTo(newActivity);

        verify(postSpec, atLeastOnce()).uri(argThat((String uriStr) -> uriStr.contains("/api/bot/notify")));
    }

    @Test
    public void testStackOverflowNoNotificationIfNoNewUpdate() throws InterruptedException {
        String link = "https://stackoverflow.com/questions/12345678/sample-question";
        long userId = 202;
        Instant previousActivity = Instant.parse("2025-03-01T15:00:00Z");
        TrackingData trackingData = new TrackingData(link, userId, new String[] {"tag"}, new String[] {"filter"}, null);
        trackingData.setLastUpdated(previousActivity);
        trackingRepository.addTracking(trackingData);
        when(stackOverflowSourceHandler.canHandle(link)).thenReturn(true);
        when(stackOverflowSourceHandler.process(any())).thenReturn(Mono.empty());

        when(gitHubSourceHandler.canHandle(link)).thenReturn(false);

        scrapperService.checkForUpdates();

        Thread.sleep(200);

        Collection<TrackingData> all = trackingRepository.getAllTracking();
        TrackingData td = all.iterator().next();
        assertThat(td.getLastUpdated()).isEqualTo(previousActivity);

        verify(postSpec, never()).uri(anyString());
    }
}
