package com.numix.web.controller;

public record CredentialsApiActionResponse(
    boolean success,
    String message,
    String redirectUrl
) {
}
