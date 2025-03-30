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
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Данные успешно сохранены"),
                @ApiResponse(responseCode = "400", description = "Неверный формат данных"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    @PostMapping("/track")
    public Mono<Void> addTracking(@RequestBody TrackingRequest trackingRequest) {
        log.info(
                "Получены данные для отслеживания: действие=добавление, ссылка={}, id пользователя={}, теги={}, фильтры={}",
                trackingRequest.getLink(),
                trackingRequest.getUserId(),
                trackingRequest.getTags(),
                trackingRequest.getFilters());
        linkService.addLink(
                trackingRequest.getLink(),
                trackingRequest.getUserId(),
                new HashSet<>(trackingRequest.getTags()),
                new HashSet<>(trackingRequest.getFilters()));
        log.info(
                "Данные успешно сохранены в репозитории: ссылка={}, id пользователя={}",
                trackingRequest.getLink(),
                trackingRequest.getUserId());
        return Mono.empty();
    }

    @Operation(
            summary = "Удаление отслеживания",
            description = "Принимает данные для прекращения отслеживания и удаляет запись из репозитория.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Запись успешно удалена или не найдена"),
                @ApiResponse(responseCode = "400", description = "Неверный формат данных"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    @PostMapping("/untrack")
    public Mono<UntrackingResponse> removeTracking(@RequestBody UntrackingRequest untrackingRequest) {
        log.info(
                "Получен запрос на прекращение отслеживания: действие=удаление, ссылка={}, id пользователя={}",
                untrackingRequest.getLink(),
                untrackingRequest.getUserId());
        boolean exists =
                linkService.deleteLinkByUserIdAndLink(untrackingRequest.getUserId(), untrackingRequest.getLink());
        if (exists) {
            log.info(
                    "Запись успешно удалена из репозитория: ссылка={}, id пользователя={}",
                    untrackingRequest.getLink(),
                    untrackingRequest.getUserId());
            return Mono.just(new UntrackingResponse(true, "Запись успешно удалена"));
        } else {
            log.info(
                    "Запись не найдена или не отслеживается: ссылка={}, id пользователя={}",
                    untrackingRequest.getLink(),
                    untrackingRequest.getUserId());
            return Mono.just(new UntrackingResponse(false, "Запись не найдена или не отслеживается"));
        }
    }
}
