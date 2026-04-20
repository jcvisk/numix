package com.numix.web.controller.view;

import com.numix.web.security.CurrentUserResolver;
import java.util.Arrays;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.numix.web.controller.view")
public class ViewControllerAdvice {

    private final CurrentUserResolver currentUserResolver;

    public ViewControllerAdvice(CurrentUserResolver currentUserResolver) {
        this.currentUserResolver = currentUserResolver;
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
}
