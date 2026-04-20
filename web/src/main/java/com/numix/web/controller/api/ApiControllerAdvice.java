package com.numix.web.controller.api;

import com.numix.core.auth.service.exception.AuthBusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.numix.web.controller.api")
public class ApiControllerAdvice {

    @ExceptionHandler(AuthBusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthBusiness(AuthBusinessException ex) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(false, ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse(false, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnhandled(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiErrorResponse(false, "Ocurrio un error interno"));
    }
}
