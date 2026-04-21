package com.numix.core.company.service.model;

public record DeleteCompanyRequest(
    Long actorUserId,
    Long companyId
) {
}
