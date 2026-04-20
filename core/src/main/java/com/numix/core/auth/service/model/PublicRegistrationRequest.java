package com.numix.core.auth.service.model;

public record PublicRegistrationRequest(
    String accountName,
    String fullName,
    String email,
    String password
) {
}
