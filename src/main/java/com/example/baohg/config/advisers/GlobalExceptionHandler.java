package com.example.baohg.config.advisers;

import com.example.baohg.dto.StandardResponse;
import com.example.baohg.exception.LogicException;
import com.example.baohg.exception.NotFoundException;
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

    @ExceptionHandler(LogicException.class)
    public ResponseEntity<?> handleLogicException(LogicException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(StandardResponse.create("400", "Bad Request", e.getMessage()));
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(StandardResponse.create("400", "Bad Request", e.getMessage()));
    }
    @ExceptionHandler(ValidateException.class)
    public ResponseEntity<?> handleValidationException(ValidateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(StandardResponse.create("400", "Bad Request", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(StandardResponse.create("400", "Bad Request", e.getMessage()));
    }
}
