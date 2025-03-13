package backend.academy.scrapper.service;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.data.TrackingRepository;
import backend.academy.scrapper.model.NotificationRequest;
import backend.academy.scrapper.model.TrackingData;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GitHubSourceHandler implements SourceHandler {

    private final GitHubClient gitHubClient;
    private final TrackingRepository trackingRepository;
    private final WebClient webClient;

    public GitHubSourceHandler(GitHubClient gitHubClient,
                               TrackingRepository trackingRepository,
                               WebClient webClient) {
        this.gitHubClient = gitHubClient;
        this.trackingRepository = trackingRepository;
        this.webClient = webClient;
    }

    @Override
    public boolean canHandle(String link) {
        return link.contains("github.com");
    }

    @Override
    public Mono<Void> process(TrackingData trackingData) {
        String link = trackingData.getLink();
        long userId = trackingData.getUserId();

        return gitHubClient.getLastUpdateTime(link)
            .timeout(Duration.ofSeconds(5))
            .doOnNext(newLastUpdate -> {
                Instant previousUpdate = trackingData.getLastUpdated();
                if (previousUpdate == null || newLastUpdate.isAfter(previousUpdate)) {
                    // Логирование и отправка уведомления
                    System.out.printf("GitHub обновление: %s, старое время: %s, новое время: %s%n",
                        link, previousUpdate, newLastUpdate);
                    sendNotification("Обновления в репозитории: " + link, userId);
                    trackingRepository.refreshLastUpdated(link, userId, newLastUpdate);
                }
            })
            .doOnError(error -> {
                System.err.printf("Ошибка при запросе к GitHub API для %s: %s%n", link, error.getMessage());
            })
            .then();
    }

    private void sendNotification(String message, long userId) {
        String botNotificationUrl = "http://localhost:8080/api/bot/notify";
        webClient.post()
            .uri(botNotificationUrl)
            .bodyValue(new NotificationRequest(message, userId))
            .retrieve()
            .bodyToMono(Void.class)
            .subscribe();
    }
}
