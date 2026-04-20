package com.numix.core.auth.service.model;

import com.numix.core.auth.entity.RoleCode;

public record UpdateAccountUserRequest(
    Long actorUserId,
    Long targetUserId,
    RoleCode roleCode,
    Boolean enabled
) {
}
