package com.nike.ncp.scheduler.exception;

import org.springframework.http.HttpStatus;

public final class ApiExceptions {

    private ApiExceptions() {
    }

    public static ApiException internalError() {
        return ApiException.builder().status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                .with(0, "Internal server error");
    }

    public static ApiException itemNotFound() {
        return ApiException.builder().status(HttpStatus.NOT_FOUND).build()
                .with(1, "Item not found");
    }

    public static ApiException accessDenied() {
        return ApiException.builder().status(HttpStatus.FORBIDDEN).build()
                .with(3, "Access denied");
    }

    public static ApiException invalidRequest() {
        return ApiException.builder().status(HttpStatus.BAD_REQUEST).build();
    }
}
