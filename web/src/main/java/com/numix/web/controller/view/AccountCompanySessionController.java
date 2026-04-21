package com.numix.web.controller.view;

import com.numix.core.auth.entity.AppUser;
import com.numix.core.auth.service.exception.AuthBusinessException;
import com.numix.web.security.CurrentUserResolver;
import com.numix.web.security.SessionCompanyContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountCompanySessionController {

    private final CurrentUserResolver currentUserResolver;
    private final SessionCompanyContext sessionCompanyContext;

    public AccountCompanySessionController(
        CurrentUserResolver currentUserResolver,
        SessionCompanyContext sessionCompanyContext
    ) {
        this.currentUserResolver = currentUserResolver;
        this.sessionCompanyContext = sessionCompanyContext;
    }

    @PostMapping("/account/company/session/select")
    public String selectCompany(
        Authentication authentication,
        HttpSession session,
        HttpServletRequest request,
        @RequestParam("companyId") Long companyId,
        RedirectAttributes redirectAttributes
    ) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);
        try {
            sessionCompanyContext.selectByCompanyId(currentUser, session, companyId);
            redirectAttributes.addFlashAttribute("successMessage", "Empresa activa actualizada");
        } catch (AuthBusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:/dashboard";
    }
}
