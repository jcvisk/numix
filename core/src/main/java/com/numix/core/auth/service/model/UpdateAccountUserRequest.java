package com.numix.core.auth.service.model;

import com.numix.core.auth.entity.RoleCode;
import java.util.Set;

public record UpdateAccountUserRequest(
    Long actorUserId,
    Long targetUserId,
    RoleCode roleCode,
    Boolean enabled,
    Set<Long> companyIds
) {
}
