package backend.academy.scrapper.service.sourceHandlers;

import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.model.dto.StackOverflowUpdate;
import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.service.linkService.LinkService;
import backend.academy.scrapper.service.notification.NotificationService;
import java.time.Instant;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class StackOverflowSourceHandler implements SourceHandler {
    private static final Logger log = LoggerFactory.getLogger(StackOverflowSourceHandler.class);
    private final StackOverflowClient stackOverflowClient;
    private final LinkService linkService;
    private final NotificationService notificationService;

    public StackOverflowSourceHandler(
            StackOverflowClient stackOverflowClient, LinkService linkService, NotificationService notificationService) {
        this.stackOverflowClient = stackOverflowClient;
        this.linkService = linkService;
        this.notificationService = notificationService;
    }

    @Override
    public boolean canHandle(String link) {
        return link.contains("stackoverflow.com");
    }

    @Override
    public Mono<Void> process(Link link) {
        String linkName = link.getLink();
        long userId = link.getUser().getTelegramId();
        String questionId = extractQuestionId(linkName);
        log.info("StackOverflowHandler обрабатывает {}, вопрос {}", linkName, questionId);

        Mono<StackOverflowUpdate> answerMono =
                stackOverflowClient.getLatestAnswer(questionId).switchIfEmpty(Mono.empty());
        Mono<StackOverflowUpdate> commentMono =
                stackOverflowClient.getLatestComment(questionId).switchIfEmpty(Mono.empty());

        log.info("Ответы: {}, комментарии: {}", answerMono, commentMono);

        return Mono.zip(answerMono, commentMono)
                .flatMap(tuple -> {
                    StackOverflowUpdate answer = tuple.getT1();
                    StackOverflowUpdate comment = tuple.getT2();
                    return Mono.just(answer.creationTime().isAfter(comment.creationTime()) ? answer : comment);
                })
                .filter(Objects::nonNull)
                .doOnNext(update -> {
                    Instant previousUpdate = link.getLastUpdated();
                    if (previousUpdate == null || update.creationTime().isAfter(previousUpdate)) {
                        log.info(
                                "Новое обновление по StackOverflow: Тема '{}', Автор {}, Создан: {}. Превью: {}",
                                update.questionTitle(),
                                update.author(),
                                update.creationTime(),
                                update.bodyPreview());
                        String message = String.format(
                                "Новое обновление по вопросу:%nТема: %s%nАвтор: %s%nСоздан: %s%nПревью: %s",
                                update.questionTitle(), update.author(), update.creationTime(), update.bodyPreview());
                        notificationService.sendNotification(message, userId);
                        linkService.refreshLastUpdated(linkName, userId, update.creationTime());
                    }
                })
                .doOnError(error -> {
                    log.error("Ошибка при запросе к StackOverflow API для {}: {}", linkName, error.getMessage());
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
        log.error("Не удалось извлечь идентификатор вопроса из URL: {}", url);
        return "";
    }
}
