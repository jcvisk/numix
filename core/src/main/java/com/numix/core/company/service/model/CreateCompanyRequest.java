package com.numix.core.company.service.model;

import java.time.LocalDate;

public record CreateCompanyRequest(
    Long actorUserId,
    String name,
    String legalName,
    String taxId,
    String fiscalRegime,
    String taxZipCode,
    Long baseCurrencyId,
    String email,
    String phone,
    String address,
    String curp,
    LocalDate birthDate
) {
}
