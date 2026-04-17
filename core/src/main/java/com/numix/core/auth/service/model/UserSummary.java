package com.numix.core.auth.service.model;

import com.numix.core.auth.entity.RoleCode;

public record UserSummary(
    Long id,
    String email,
    String fullName,
    RoleCode roleCode,
    boolean enabled
) {
}
