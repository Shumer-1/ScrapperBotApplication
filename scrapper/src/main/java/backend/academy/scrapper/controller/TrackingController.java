package backend.academy.scrapper.controller;

import backend.academy.scrapper.model.dto.TrackingRequest;
import backend.academy.scrapper.model.dto.UntrackingRequest;
import backend.academy.scrapper.model.dto.UntrackingResponse;
import backend.academy.scrapper.service.linkService.LinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.HashSet;
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
    private final LinkService linkService;

    public TrackingController(LinkService linkService) {
        this.linkService = linkService;
    }

    @Operation(
        summary = "Добавление отслеживания",
        description = "Принимает данные для отслеживания и сохраняет их в репозитории.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Данные успешно сохранены"),
        @ApiResponse(responseCode = "400", description = "Неверный формат данных"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/track")
    public Mono<Void> addTracking(@RequestBody TrackingRequest trackingRequest) {
        log.info("Получены данные для отслеживания: действие=добавление, ссылка={}, id пользователя={}, теги={}, фильтры={}",
            trackingRequest.link(), trackingRequest.userId(), trackingRequest.tags(), trackingRequest.filters());
        return Mono.fromRunnable(() ->
            linkService.addLink(
                trackingRequest.link(),
                trackingRequest.userId(),
                new HashSet<>(trackingRequest.tags()),
                new HashSet<>(trackingRequest.filters()))
        );
    }

    @Operation(
        summary = "Удаление отслеживания",
        description = "Принимает данные для прекращения отслеживания и удаляет запись из репозитория.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Запись успешно удалена"),
        @ApiResponse(responseCode = "400", description = "Неверный формат данных"),
        @ApiResponse(responseCode = "404", description = "Запись не найдена"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/untrack")
    public Mono<UntrackingResponse> removeTracking(@RequestBody UntrackingRequest untrackingRequest) {
        log.info("Получен запрос на прекращение отслеживания: действие=удаление, ссылка={}, id пользователя={}",
            untrackingRequest.link(), untrackingRequest.userId());
        return Mono.fromCallable(() -> {
            linkService.deleteLinkByUserIdAndLink(untrackingRequest.userId(), untrackingRequest.link());
            return new UntrackingResponse(true, "Запись успешно удалена");
        });
    }
}
