package backend.academy.scrapper.service;

import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.data.TrackingRepository;
import backend.academy.scrapper.model.NotificationRequest;
import backend.academy.scrapper.model.TrackingData;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class StackOverflowSourceHandler implements SourceHandler {

    private final StackOverflowClient stackOverflowClient;
    private final TrackingRepository trackingRepository;
    private final WebClient webClient;

    public StackOverflowSourceHandler(
            StackOverflowClient stackOverflowClient, TrackingRepository trackingRepository, WebClient webClient) {
        this.stackOverflowClient = stackOverflowClient;
        this.trackingRepository = trackingRepository;
        this.webClient = webClient;
    }

    @Override
    public boolean canHandle(String link) {
        return link.contains("stackoverflow.com");
    }

    @Override
    public Mono<Void> process(TrackingData trackingData) {
        String link = trackingData.getLink();
        long userId = trackingData.getUserId();
        String questionId = extractQuestionId(link);

        return stackOverflowClient
                .getQuestionLastActivity(questionId)
                .timeout(Duration.ofSeconds(5))
                .doOnNext(newLastActivity -> {
                    Instant previousUpdate = trackingData.getLastUpdated();
                    if (previousUpdate == null || newLastActivity.isAfter(previousUpdate)) {
                        System.out.printf(
                                "StackOverflow обновление: %s, старое время: %s, новое время: %s%n",
                                link, previousUpdate, newLastActivity);
                        sendNotification("Обновления по вопросу: " + link, userId);
                        trackingRepository.refreshLastUpdated(link, userId, newLastActivity);
                    }
                })
                .doOnError(error -> {
                    System.err.printf("Ошибка при запросе к StackOverflow API для %s: %s%n", link, error.getMessage());
                })
                .then();
    }

    private String extractQuestionId(String url) {
        String[] parts = url.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("questions".equals(parts[i]) && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        System.err.printf("Не удалось извлечь идентификатор вопроса из URL: %s%n", url);
        return "";
    }

    private void sendNotification(String message, long userId) {
        String botNotificationUrl = "http://localhost:8080/api/bot/notify";
        webClient
                .post()
                .uri(botNotificationUrl)
                .bodyValue(new NotificationRequest(message, userId))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }
}
