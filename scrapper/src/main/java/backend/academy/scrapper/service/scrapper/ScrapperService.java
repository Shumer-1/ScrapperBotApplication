package backend.academy.scrapper.service.scrapper;

import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.service.linkService.LinkService;
import backend.academy.scrapper.service.sourceHandlers.SourceHandler;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
public class ScrapperService {

    private final List<SourceHandler> sourceHandlers;
    private final LinkService linkService;
    private static final int PAGE_SIZE = 100;
    private static final int PARALLELISM = 4;
    private static final Logger log = LoggerFactory.getLogger(ScrapperService.class);

    public ScrapperService(List<SourceHandler> sourceHandlers, LinkService linkService) {
        this.sourceHandlers = sourceHandlers;
        this.linkService = linkService;
    }

    @Scheduled(fixedRateString = "${app.scrapper.pollingInterval:5000}")
    public void checkForUpdates() {
        log.info("Запуск проверки обновлений");
        int page = 0;
        Page<Link> trackingPage;
        do {
            trackingPage = linkService.getTrackingPage(page, PAGE_SIZE);
            log.info("Обрабатывается страница {}, записей: {}", page, trackingPage.getNumberOfElements());

            Flux.fromIterable(trackingPage.getContent())
                    .parallel(PARALLELISM)
                    .runOn(Schedulers.parallel())
                    .flatMap(link -> Flux.fromIterable(sourceHandlers)
                            .filter(handler -> handler.canHandle(link.getLink()))
                            .flatMap(handler -> handler.process(link)))
                    .sequential()
                    .subscribe(
                            unused -> {}, error -> log.error("Ошибка при обработке страницы: {}", error.getMessage()));
            page++;
        } while (trackingPage.hasNext());
    }
}
