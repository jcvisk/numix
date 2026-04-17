package com.numix.web.controller;

import com.numix.core.auth.entity.AppUser;
import com.numix.core.auth.entity.RoleCode;
import com.numix.core.auth.service.AppUserService;
import com.numix.core.auth.service.exception.AuthBusinessException;
import com.numix.core.auth.service.model.CreateAccountUserRequest;
import com.numix.core.auth.service.model.UpdateAccountUserRequest;
import com.numix.web.controller.form.CreateUserForm;
import com.numix.web.controller.form.UpdateUserForm;
import com.numix.web.security.CurrentUserResolver;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountUsersController {

    private final AppUserService appUserService;
    private final CurrentUserResolver currentUserResolver;

    public AccountUsersController(AppUserService appUserService, CurrentUserResolver currentUserResolver) {
        this.appUserService = appUserService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/account/users")
    public String usersPage(Authentication authentication, Model model) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);

        if (!model.containsAttribute("createForm")) {
            model.addAttribute("createForm", new CreateUserForm());
        }
        if (!model.containsAttribute("updateForm")) {
            model.addAttribute("updateForm", new UpdateUserForm());
        }

        model.addAttribute("users", appUserService.listAccountUsers(currentUser.getId()));
        model.addAttribute("roles", manageableAssignableRoles());
        return "account/users";
    }

    @PostMapping("/account/users/create")
    public String createUser(
        Authentication authentication,
        @ModelAttribute("createForm") CreateUserForm createForm,
        RedirectAttributes redirectAttributes
    ) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);
        try {
            appUserService.createAccountUser(new CreateAccountUserRequest(
                currentUser.getId(),
                createForm.getEmail(),
                createForm.getPassword(),
                createForm.getFullName(),
                createForm.getRoleCode()
            ));
            redirectAttributes.addFlashAttribute("successMessage", "Usuario creado correctamente");
        } catch (AuthBusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("createForm", createForm);
        }
        return "redirect:/account/users";
    }

    @PostMapping("/account/users/update")
    public String updateUser(
        Authentication authentication,
        @ModelAttribute("updateForm") UpdateUserForm updateForm,
        RedirectAttributes redirectAttributes
    ) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);
        try {
            appUserService.updateAccountUser(new UpdateAccountUserRequest(
                currentUser.getId(),
                updateForm.getTargetUserId(),
                updateForm.getRoleCode(),
                updateForm.getEnabled()
            ));
            redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado correctamente");
        } catch (AuthBusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/account/users";
    }

    private List<RoleCode> manageableAssignableRoles() {
        return List.of(RoleCode.ADMIN, RoleCode.AUX);
    }
}
