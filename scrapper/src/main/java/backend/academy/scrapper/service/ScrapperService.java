package backend.academy.scrapper.service;

import backend.academy.scrapper.data.TrackingRepository;
import backend.academy.scrapper.model.TrackingData;
import java.util.Collection;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ScrapperService {

    private final List<SourceHandler> sourceHandlers;
    private final TrackingRepository trackingRepository;

    public ScrapperService(List<SourceHandler> sourceHandlers, TrackingRepository trackingRepository) {
        this.sourceHandlers = sourceHandlers;
        this.trackingRepository = trackingRepository;
    }

    @Scheduled(fixedRateString = "${app.scrapper.pollingInterval:5000}")
    public void checkForUpdates() {
        System.out.println("Запуск проверки обновлений");
        Collection<TrackingData> allTracking = trackingRepository.getAllTracking();
        System.out.printf("Всего отслеживаемых записей: %d%n", allTracking.size());

        Flux.fromIterable(allTracking)
                .flatMap(trackingData -> Flux.fromIterable(sourceHandlers)
                        .filter(handler -> handler.canHandle(trackingData.getLink()))
                        .flatMap(handler -> handler.process(trackingData))
                        .next())
                .subscribe();
    }
}
