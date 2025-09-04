package com.haderacher.bcbackend.handler;

import com.haderacher.bcbackend.exception.UserExistException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleInvalidAuthentication(BadCredentialsException e) {
        ErrorResponse errorResponse = new ErrorResponse("Authentication Failed", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(UserExistException.class)
    public ResponseEntity<Object> handleUserExist(UserExistException e) {
        ErrorResponse errorResponse = new ErrorResponse("Authentication Failed", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}

@Data
@AllArgsConstructor
class ErrorResponse {
    private String error;
    private String message;
}
