package com.numix.web.controller.api;

public record ApiErrorResponse(
    boolean success,
    String message
) {
}
