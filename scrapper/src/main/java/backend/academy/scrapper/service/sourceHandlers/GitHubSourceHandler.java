package backend.academy.scrapper.service.sourceHandlers;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.service.linkService.LinkService;
import backend.academy.scrapper.service.notification.NotificationService;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GitHubSourceHandler implements SourceHandler {

    private static final Logger log = LoggerFactory.getLogger(GitHubSourceHandler.class);
    private final GitHubClient gitHubClient;
    private final NotificationService notificationService;
    private final LinkService linkService;

    public GitHubSourceHandler(
            GitHubClient gitHubClient, NotificationService notificationService, LinkService linkService) {
        this.gitHubClient = gitHubClient;
        this.notificationService = notificationService;
        this.linkService = linkService;
    }

    @Override
    public boolean canHandle(String link) {
        return link.contains("github.com");
    }

    @Override
    public Mono<Void> process(Link link) {
        log.info("Может быть обработан GithubHandler");
        String linkName = link.getLink();
        long userId = link.getUser().getTelegramId();
        log.info("GithubHandler обрабатывает {} от {}", linkName, userId);
        return gitHubClient
                .getLatestIssueOrPR(linkName)
                .timeout(Duration.ofSeconds(5))
                .doOnNext(issue -> {
                    if (link.getLastUpdated() == null || issue.createdAt().isAfter(link.getLastUpdated())) {
                        log.info("Старое значение {}, новое значение: {} ", link.getLastUpdated(), issue.createdAt());
                        log.info(
                                "Новый PR/Issue обновлен: '{}' от пользователя {} в {}. Превью: {}",
                                issue.title(),
                                issue.username(),
                                issue.createdAt(),
                                issue.bodyPreview());
                        String message = String.format(
                                "Новый PR/Issue:%nНазвание: %s%nАвтор: %s%nСоздан: %s%nОписание: %s",
                                issue.title(), issue.username(), issue.createdAt(), issue.bodyPreview());
                        notificationService.sendNotification(message, userId);
                        linkService.refreshLastUpdated(linkName, userId, issue.createdAt());
                    }
                })
                .doOnError(error -> {
                    log.error("Ошибка при запросе к GitHub API для {}: {}%n", linkName, error.getMessage());
                })
                .then();
    }
}
