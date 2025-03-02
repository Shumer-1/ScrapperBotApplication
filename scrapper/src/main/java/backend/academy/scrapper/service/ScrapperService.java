package backend.academy.scrapper.service;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.data.TrackingRepository;
import backend.academy.scrapper.model.NotificationRequest;
import backend.academy.scrapper.model.TrackingData;
import java.time.Instant;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ScrapperService {

    private static final Logger log = LoggerFactory.getLogger(ScrapperService.class);

    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final WebClient webClient;
    private final TrackingRepository trackingRepository;

    public ScrapperService(GitHubClient gitHubClient,
                           StackOverflowClient stackOverflowClient,
                           WebClient webClient,
                           TrackingRepository trackingRepository) {
        this.gitHubClient = gitHubClient;
        this.stackOverflowClient = stackOverflowClient;
        this.webClient = webClient;
        this.trackingRepository = trackingRepository;
    }

    @Scheduled(fixedRateString = "${app.scrapper.pollingInterval:5000}")
    public void checkForUpdates() {
        log.info("Запуск проверки обновлений");
        Collection<TrackingData> allTracking = trackingRepository.getAllTracking();
        log.debug("Всего отслеживаемых записей: {}", allTracking.size());

        for (TrackingData trackingData : allTracking) {
            String link = trackingData.getLink();
            long userId = trackingData.getUserId();

            if (link.contains("github.com")) {
                gitHubClient.getLastUpdateTime(link)
                    .doOnNext(newLastUpdate -> {
                        Instant previousUpdate = trackingData.getLastUpdated();
                        if (previousUpdate == null || newLastUpdate.isAfter(previousUpdate)) {
                            log.info("Обнаружены изменения в репозитории: действие={}, ссылка={}, старое время={}, новое время={}",
                                "обновление", link, previousUpdate, newLastUpdate);
                            sendNotification("Обновления в репозитории: " + link, userId);
                            trackingRepository.updateLastUpdated(link, userId, newLastUpdate);
                        } else {
                            log.debug("Изменений не обнаружено для репозитория: ссылка={}, старое время={}, новое время={}",
                                link, previousUpdate, newLastUpdate);
                        }
                    })
                    .doOnError(error -> log.error("Ошибка при запросе к GitHub API: действие={}, ссылка={}, ошибка={}",
                        "запрос", link, error.getMessage(), error))
                    .subscribe();
            } else if (link.contains("stackoverflow.com")) {
                String questionId = extractQuestionId(link);
                stackOverflowClient.getQuestionLastActivity(questionId)
                    .doOnNext(newLastActivity -> {
                        Instant previousUpdate = trackingData.getLastUpdated();
                        if (previousUpdate == null || newLastActivity.isAfter(previousUpdate)) {
                            log.info("Обнаружены изменения по вопросу: действие={}, ссылка={}, старое время={}, новое время={}",
                                "обновление", link, previousUpdate, newLastActivity);
                            sendNotification("Обновления по вопросу: " + link, userId);
                            trackingRepository.updateLastUpdated(link, userId, newLastActivity);
                        } else {
                            log.debug("Изменений не обнаружено для вопроса: ссылка={}, старое время={}, новое время={}",
                                link, previousUpdate, newLastActivity);
                        }
                    })
                    .doOnError(error -> log.error("Ошибка при запросе к StackOverflow API: действие={}, ссылка={}, ошибка={}",
                        "запрос", link, error.getMessage(), error))
                    .subscribe();
            }
        }
    }

    private void sendNotification(String message, long userId) {
        String botNotificationUrl = "http://localhost:8080/api/bot/notify";
        webClient.post()
            .uri(botNotificationUrl)
            .bodyValue(new NotificationRequest(message, userId))
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(aVoid -> log.info("Уведомление отправлено: действие={}, сообщение={}, id пользователя={}",
                "отправка", message, userId))
            .doOnError(error -> log.error("Ошибка при отправке уведомления в бот: действие={}, сообщение={}, id пользователя={}, ошибка={}",
                "отправка", message, userId, error.getMessage(), error))
            .subscribe();
    }

    private String extractQuestionId(String url) {
        String[] parts = url.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("questions".equals(parts[i]) && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        log.warn("Не удалось извлечь идентификатор вопроса из URL: {}", url);
        return "";
    }
}
