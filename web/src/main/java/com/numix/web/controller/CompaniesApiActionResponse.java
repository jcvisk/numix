package com.numix.web.controller;

public record CompaniesApiActionResponse(
    boolean success,
    String message,
    CompaniesPageApiResponse data
) {
}
