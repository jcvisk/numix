package com.numix.web.security;

import com.numix.core.auth.entity.AppUser;
import com.numix.core.auth.service.exception.AuthBusinessException;
import com.numix.core.company.service.CompanyService;
import com.numix.core.company.service.model.CompanySummary;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SessionCompanyContext {

    public static final String SESSION_ACTIVE_COMPANY_ID = "ACTIVE_COMPANY_ID";
    public static final String SESSION_ACTIVE_COMPANY_NAME = "ACTIVE_COMPANY_NAME";

    private final CompanyService companyService;

    public SessionCompanyContext(CompanyService companyService) {
        this.companyService = companyService;
    }

    public SessionCompanySelection synchronize(AppUser user, HttpSession session) {
        List<CompanySummary> visibleCompanies = companyService.listVisibleCompanies(user.getId());
        if (visibleCompanies.isEmpty()) {
            clear(session);
            return new SessionCompanySelection(List.of(), null, null);
        }

        Long sessionCompanyId = sessionCompanyId(session);
        CompanySummary activeCompany = visibleCompanies.stream()
            .filter(company -> company.id().equals(sessionCompanyId))
            .findFirst()
            .orElse(visibleCompanies.getFirst());

        setActive(session, activeCompany);
        return new SessionCompanySelection(visibleCompanies, activeCompany.id(), activeCompany.name());
    }

    public SessionCompanySelection selectByCompanyId(AppUser user, HttpSession session, Long companyId) {
        if (companyId == null || companyId <= 0) {
            throw new AuthBusinessException("Empresa inválida");
        }
        List<CompanySummary> visibleCompanies = companyService.listVisibleCompanies(user.getId());
        CompanySummary selected = visibleCompanies.stream()
            .filter(company -> company.id().equals(companyId))
            .findFirst()
            .orElseThrow(() -> new AuthBusinessException("La empresa no está disponible para este usuario"));

        setActive(session, selected);
        return new SessionCompanySelection(visibleCompanies, selected.id(), selected.name());
    }

    public Optional<Long> activeCompanyId(HttpSession session) {
        return Optional.ofNullable(sessionCompanyId(session));
    }

    public Optional<String> activeCompanyName(HttpSession session) {
        Object value = session.getAttribute(SESSION_ACTIVE_COMPANY_NAME);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value.toString());
    }

    private Long sessionCompanyId(HttpSession session) {
        Object value = session.getAttribute(SESSION_ACTIVE_COMPANY_ID);
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String stringValue) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private void setActive(HttpSession session, CompanySummary company) {
        session.setAttribute(SESSION_ACTIVE_COMPANY_ID, company.id());
        session.setAttribute(SESSION_ACTIVE_COMPANY_NAME, company.name());
    }

    private void clear(HttpSession session) {
        session.removeAttribute(SESSION_ACTIVE_COMPANY_ID);
        session.removeAttribute(SESSION_ACTIVE_COMPANY_NAME);
    }

    public record SessionCompanySelection(
        List<CompanySummary> companies,
        Long activeCompanyId,
        String activeCompanyName
    ) {
    }
}
