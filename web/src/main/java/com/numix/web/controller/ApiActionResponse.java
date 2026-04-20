package com.numix.web.controller;

public record ApiActionResponse(
    boolean success,
    String message,
    UsersPageApiResponse data
) {
}
