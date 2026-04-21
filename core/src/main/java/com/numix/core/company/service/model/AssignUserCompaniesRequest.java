package com.numix.core.company.service.model;

import java.util.Set;

public record AssignUserCompaniesRequest(
    Long actorUserId,
    Long targetUserId,
    Set<Long> companyIds
) {
}
