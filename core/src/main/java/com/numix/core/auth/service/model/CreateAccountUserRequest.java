package com.numix.core.auth.service.model;

import com.numix.core.auth.entity.RoleCode;
import java.util.Set;

public record CreateAccountUserRequest(
    Long actorUserId,
    String email,
    String password,
    String fullName,
    RoleCode roleCode,
    Set<Long> companyIds
) {
}
