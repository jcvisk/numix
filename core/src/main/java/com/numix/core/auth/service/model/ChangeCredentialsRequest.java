package com.numix.core.auth.service.model;

public record ChangeCredentialsRequest(
    Long userId,
    String currentPassword,
    String newEmail,
    String newPassword
) {
}
