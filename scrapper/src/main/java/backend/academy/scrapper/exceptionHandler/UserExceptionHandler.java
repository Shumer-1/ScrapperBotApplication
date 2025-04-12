package backend.academy.scrapper.exceptionHandler;

import backend.academy.scrapper.exceptions.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "backend.academy.scrapper.controller.user")
public class UserExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<GlobalExceptionHandler.ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new GlobalExceptionHandler.ErrorResponse("Пользователь уже существует", ex.getMessage()));
    }
}
