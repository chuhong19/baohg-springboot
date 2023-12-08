package com.example.baohg.config.advisers;

import com.example.baohg.dto.StandardResponse;
import com.example.baohg.exception.ValidateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@CrossOrigin
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("handleException", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.create("500", "ServerError", e.getLocalizedMessage()));
    }

    @ExceptionHandler(ValidateException.class)
    public ResponseEntity<?> handleValidationException(ValidateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(StandardResponse.create("400", "BadRequest", e.getMessage()));
    }
}
