package backend.academy.scrapper.exceptionHandler;

import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DataAccessExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GlobalExceptionHandler.ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new GlobalExceptionHandler.ErrorResponse("Запись не найдена", ex.getMessage()));
    }
}

