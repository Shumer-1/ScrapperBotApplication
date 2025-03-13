package backend.academy.scrapper.controller;

import backend.academy.scrapper.data.TrackingRepository;
import backend.academy.scrapper.model.TrackingData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/scrapper")
public class TrackingController {

    private static final Logger log = LoggerFactory.getLogger(TrackingController.class);
    private final TrackingRepository trackingRepository;

    public TrackingController(TrackingRepository trackingRepository) {
        this.trackingRepository = trackingRepository;
    }

    @Operation(
        summary = "Добавление отслеживания",
        description = "Принимает данные для отслеживания и сохраняет их в репозитории."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Данные успешно сохранены"),
        @ApiResponse(responseCode = "400", description = "Неверный формат данных"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/track")
    public Mono<Void> addTracking(@RequestBody TrackingData trackingData) {
        log.info(
            "Получены данные для отслеживания: действие={}, ссылка={}, id пользователя={}, теги={}, фильтры={}",
            "добавление",
            trackingData.getLink(),
            trackingData.getUserId(),
            trackingData.getTags(),
            trackingData.getFilters());
        trackingRepository.addTracking(trackingData);
        log.info(
            "Данные успешно сохранены в репозитории: ссылка={}, id пользователя={}",
            trackingData.getLink(),
            trackingData.getUserId());
        return Mono.empty();
    }

    @Operation(
        summary = "Удаление отслеживания",
        description = "Принимает данные для прекращения отслеживания и удаляет запись из репозитория."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Запись успешно удалена"),
        @ApiResponse(responseCode = "400", description = "Неверный формат данных"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/untrack")
    public Mono<Void> removeTracking(@RequestBody TrackingData trackingData) {
        log.info(
            "Получен запрос на прекращение отслеживания: действие={}, ссылка={}, id пользователя={}",
            "удаление",
            trackingData.getLink(),
            trackingData.getUserId());
        trackingRepository.removeTracking(trackingData);
        log.info(
            "Запись успешно удалена из репозитория: ссылка={}, id пользователя={}",
            trackingData.getLink(),
            trackingData.getUserId());
        return Mono.empty();
    }
}
