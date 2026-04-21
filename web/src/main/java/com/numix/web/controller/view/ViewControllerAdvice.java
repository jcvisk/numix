package com.numix.web.controller.view;

import com.numix.web.security.CurrentUserResolver;
import com.numix.web.security.SessionCompanyContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Arrays;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.util.UriComponentsBuilder;

@ControllerAdvice(basePackages = "com.numix.web.controller.view")
public class ViewControllerAdvice {

    private final CurrentUserResolver currentUserResolver;
    private final SessionCompanyContext sessionCompanyContext;

    public ViewControllerAdvice(CurrentUserResolver currentUserResolver, SessionCompanyContext sessionCompanyContext) {
        this.currentUserResolver = currentUserResolver;
        this.sessionCompanyContext = sessionCompanyContext;
    }

    @ModelAttribute
    public void currentUserDisplayName(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            model.addAttribute("currentUserFullName", "");
            model.addAttribute("currentUserNamePart1", "Usuario");
            model.addAttribute("currentUserNamePart2", "");
            return;
        }

        String fullName;
        try {
            fullName = currentUserResolver.resolveOrFail(authentication).getFullName();
        } catch (RuntimeException ex) {
            fullName = authentication.getName();
        }

        String normalizedFullName = fullName == null ? "" : fullName.trim().replaceAll("\\s+", " ");
        String[] parts = normalizedFullName.isEmpty() ? new String[0] : Arrays.stream(normalizedFullName.split(" "))
            .filter(part -> !part.isBlank())
            .toArray(String[]::new);

        String part1 = parts.length > 0 ? parts[0] : "Usuario";
        String part2 = parts.length > 1 ? parts[1] : "";

        model.addAttribute("currentUserFullName", normalizedFullName);
        model.addAttribute("currentUserNamePart1", part1);
        model.addAttribute("currentUserNamePart2", part2);
    }

    @ModelAttribute
    public void languageSwitcher(Locale locale, HttpServletRequest request, Model model) {
        String currentLanguage = locale == null ? "es" : locale.getLanguage();
        String normalizedCurrentLanguage = "en".equalsIgnoreCase(currentLanguage) ? "en" : "es";

        model.addAttribute("currentLanguage", normalizedCurrentLanguage);
        model.addAttribute("languageSwitchEsUrl", buildLanguageSwitchUrl(request, "es"));
        model.addAttribute("languageSwitchEnUrl", buildLanguageSwitchUrl(request, "en"));
    }

    @ModelAttribute
    public void activeCompany(Authentication authentication, HttpSession session, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            model.addAttribute("headerCompanies", java.util.List.of());
            model.addAttribute("activeCompanyName", "Sin empresa");
            model.addAttribute("activeCompanyId", null);
            model.addAttribute("hasActiveCompany", false);
            return;
        }

        try {
            var currentUser = currentUserResolver.resolveOrFail(authentication);
            var selection = sessionCompanyContext.synchronize(currentUser, session);
            model.addAttribute("headerCompanies", selection.companies());
            model.addAttribute("activeCompanyName", selection.activeCompanyName() == null ? "Sin empresa" : selection.activeCompanyName());
            model.addAttribute("activeCompanyId", selection.activeCompanyId());
            model.addAttribute("hasActiveCompany", selection.activeCompanyId() != null);
        } catch (RuntimeException ex) {
            model.addAttribute("headerCompanies", java.util.List.of());
            model.addAttribute("activeCompanyName", "Sin empresa");
            model.addAttribute("activeCompanyId", null);
            model.addAttribute("hasActiveCompany", false);
        }
    }

    private String buildLanguageSwitchUrl(HttpServletRequest request, String languageTag) {
        String requestUri = request == null ? "/" : request.getRequestURI();
        if (requestUri == null || requestUri.isBlank()) {
            requestUri = "/";
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(requestUri);
        if (request != null) {
            request.getParameterMap().forEach((param, values) -> {
                if ("lang".equalsIgnoreCase(param) || values == null) {
                    return;
                }
                for (String value : values) {
                    builder.queryParam(param, value);
                }
            });
        }

        return builder.replaceQueryParam("lang", languageTag).build(true).toUriString();
    }
}
