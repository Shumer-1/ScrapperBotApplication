package backend.academy.scrapper.controller;

import backend.academy.scrapper.model.dto.UserRegistrationRequest;
import backend.academy.scrapper.model.dto.UserRegistrationResponse;
import backend.academy.scrapper.service.userService.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/scrapper")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Регистрация пользователя",
            description =
                    "Принимает данные для регистрации пользователя и регистрирует его, если он еще не существует.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
                @ApiResponse(responseCode = "400", description = "Неверный формат запроса"),
                @ApiResponse(responseCode = "409", description = "Пользователь уже существует"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    @PostMapping("/user")
    public Mono<ResponseEntity<UserRegistrationResponse>> registerUser(@RequestBody UserRegistrationRequest request) {
        log.info(
                "Получен запрос на регистрацию пользователя: id={}, username={}", request.userId(), request.username());
        return Mono.fromRunnable(() -> userService.save(request.userId(), request.username()))
                .then(Mono.just(
                        ResponseEntity.ok(new UserRegistrationResponse(true, "Пользователь зарегистрирован успешно"))));
    }
}
