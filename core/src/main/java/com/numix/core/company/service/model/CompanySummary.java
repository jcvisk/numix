package com.numix.core.company.service.model;

import com.numix.core.company.entity.CompanyStatus;
import java.time.LocalDate;

public record CompanySummary(
    Long id,
    String name,
    String legalName,
    String taxId,
    String fiscalRegime,
    String taxZipCode,
    Long baseCurrencyId,
    String baseCurrencyCode,
    String email,
    String phone,
    String address,
    String curp,
    LocalDate birthDate,
    CompanyStatus status
) {
}
