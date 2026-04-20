package com.numix.web.controller;

import com.numix.core.auth.entity.AppUser;
import com.numix.core.auth.service.AppUserService;
import com.numix.core.auth.service.exception.AuthBusinessException;
import com.numix.core.auth.service.model.ChangeCredentialsRequest;
import com.numix.web.controller.form.CredentialsForm;
import com.numix.web.security.CurrentUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final AppUserService appUserService;
    private final CurrentUserResolver currentUserResolver;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    public ProfileController(AppUserService appUserService, CurrentUserResolver currentUserResolver) {
        this.appUserService = appUserService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/profile/credentials")
    public String credentialsPage(Authentication authentication, Model model) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);
        if (!model.containsAttribute("form")) {
            CredentialsForm form = new CredentialsForm();
            form.setNewEmail(currentUser.getEmail());
            form.setConfirmEmail(currentUser.getEmail());
            model.addAttribute("form", form);
        }
        model.addAttribute("currentEmail", currentUser.getEmail());
        return "profile/credentials";
    }

    @PostMapping("/profile/credentials")
    public String changeCredentials(
        Authentication authentication,
        HttpServletRequest request,
        HttpServletResponse response,
        @ModelAttribute("form") CredentialsForm form,
        RedirectAttributes redirectAttributes
    ) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);
        if (!sameValue(form.getNewEmail(), form.getConfirmEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Los correos no coinciden");
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/profile/credentials";
        }
        if (!sameValue(form.getNewPassword(), form.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Las contraseñas no coinciden");
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/profile/credentials";
        }

        try {
            appUserService.changeCredentials(new ChangeCredentialsRequest(
                currentUser.getId(),
                form.getCurrentPassword(),
                form.getNewEmail(),
                form.getNewPassword()
            ));
        } catch (AuthBusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/profile/credentials";
        }

        redirectAttributes.addFlashAttribute(
            "successMessage",
            "Credenciales actualizadas. Inicia sesión nuevamente con tu nuevo correo."
        );
        logoutHandler.logout(request, response, authentication);
        return "redirect:/login";
    }

    @GetMapping("/profile/credentials/api/data")
    @ResponseBody
    public CredentialsApiData credentialsData(Authentication authentication) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);
        return new CredentialsApiData(currentUser.getEmail());
    }

    @PostMapping("/profile/credentials/api/change")
    @ResponseBody
    public CredentialsApiActionResponse changeCredentialsApi(
        Authentication authentication,
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody CredentialsForm form
    ) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);
        if (!sameValue(form.getNewEmail(), form.getConfirmEmail())) {
            return new CredentialsApiActionResponse(false, "Los correos no coinciden", null);
        }
        if (!sameValue(form.getNewPassword(), form.getConfirmPassword())) {
            return new CredentialsApiActionResponse(false, "Las contraseñas no coinciden", null);
        }

        try {
            appUserService.changeCredentials(new ChangeCredentialsRequest(
                currentUser.getId(),
                form.getCurrentPassword(),
                form.getNewEmail(),
                form.getNewPassword()
            ));
        } catch (AuthBusinessException ex) {
            return new CredentialsApiActionResponse(false, ex.getMessage(), null);
        }

        logoutHandler.logout(request, response, authentication);
        return new CredentialsApiActionResponse(
            true,
            "Credenciales actualizadas. Inicia sesión nuevamente con tu nuevo correo.",
            "/login"
        );
    }

    private boolean sameValue(String left, String right) {
        return left != null && left.equals(right);
    }
}
