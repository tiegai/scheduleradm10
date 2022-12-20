package com.nike.ncp.scheduler.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    protected ResponseEntity<Object> handleApiException(RuntimeException ex, WebRequest request) {
        ApiException exception = (ApiException) ex;
        log.warn("api_exception, code={}, errors={}", exception.getStatus().value(), exception.getErrors());
        ApiExceptionResponse response = ApiExceptionResponse.builder().errors(exception.getErrors()).build();
        return handleExceptionInternal(ex, response, new HttpHeaders(), exception.getStatus(), request);
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<Object> handleInternalException(RuntimeException ex, WebRequest request) {
        log.error("internal_exception, ex={}", ex.getMessage(), ex);
        ApiException exception = ApiExceptions.internalError();
        ApiExceptionResponse response = ApiExceptionResponse.builder().errors(exception.getErrors()).build();
        return handleExceptionInternal(ex, response, new HttpHeaders(), exception.getStatus(), request);
    }
}
