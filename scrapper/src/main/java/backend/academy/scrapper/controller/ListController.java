package backend.academy.scrapper.controller;

import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.service.linkService.LinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/scrapper")
public class ListController {

    private static final Logger log = LoggerFactory.getLogger(ListController.class);
    private final LinkService linkService;

    public ListController(LinkService linkService) {
        this.linkService = linkService;
    }

    @Operation(
            summary = "Получение списка ссылок",
            description = "Возвращает список ссылок, отслеживаемых пользователем по его идентификатору (userId).")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Ссылки успешно получены"),
                @ApiResponse(responseCode = "400", description = "Неверный формат запроса"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    @GetMapping("/links")
    public Flux<Link> getLinks(@RequestParam("userId") long userId) {
        log.info("Получение списка ссылок для пользователя: {}", userId);
        List<Link> links = linkService.getLinksByUserId(userId);
        return Flux.fromIterable(links);
    }
}
