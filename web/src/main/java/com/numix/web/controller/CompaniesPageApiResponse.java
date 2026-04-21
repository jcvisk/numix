package com.numix.web.controller;

import com.numix.core.company.service.model.CompanySummary;
import com.numix.core.company.service.model.CurrencySummary;
import java.util.List;

public record CompaniesPageApiResponse(
    List<CompanySummary> companies,
    List<CurrencySummary> currencies
) {
}
