package com.numix.web.controller.view;

import com.numix.core.auth.service.AppUserService;
import com.numix.core.auth.service.exception.AuthBusinessException;
import com.numix.core.auth.service.model.PublicRegistrationRequest;
import com.numix.web.controller.form.RegisterForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController extends BaseViewController {

    private final AppUserService appUserService;

    public AuthController(AppUserService appUserService) {
        super("auth");
        this.appUserService = appUserService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegisterForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(
        @ModelAttribute("form") RegisterForm form,
        RedirectAttributes redirectAttributes
    ) {
        if (!sameValue(form.getEmail(), form.getConfirmEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Los correos no coinciden");
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/register";
        }
        if (!sameValue(form.getPassword(), form.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Las contraseñas no coinciden");
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/register";
        }

        try {
            appUserService.registerPublicOwner(new PublicRegistrationRequest(
                form.getAccountName(),
                form.getFullName(),
                form.getEmail(),
                form.getPassword()
            ));
        } catch (AuthBusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/register";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cuenta creada. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    private boolean sameValue(String left, String right) {
        return left != null && left.equals(right);
    }
}
