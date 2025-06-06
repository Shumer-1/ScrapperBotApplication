package backend.academy.bot.controller;

import backend.academy.bot.model.dto.NotificationRequest;
import backend.academy.bot.services.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bot")
@Validated
public class BotNotificationController {
    private static final Logger log = LoggerFactory.getLogger(BotNotificationController.class);
    private final NotificationService notificationService;

    public BotNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(
            summary = "Receive Notification",
            description = "Принимает уведомление от скраппера и передаёт его в NotificationService для обработки.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Уведомление успешно принято и обрабатывается."),
                @ApiResponse(responseCode = "400", description = "Неверный формат запроса."),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            })
    @PostMapping("/notify")
    public CompletableFuture<ResponseEntity<Void>> receiveNotification(
            @Valid @RequestBody NotificationRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info(
                    "Получено уведомление от скраппера: действие={}, сообщение={}, id пользователя={}",
                    "получено",
                    request.getMessage(),
                    request.getUserId());
            notificationService.notify(request.getMessage(), request.getUserId());
            return ResponseEntity.ok().build();
        });
    }
}
