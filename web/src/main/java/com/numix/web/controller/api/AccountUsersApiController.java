package com.numix.web.controller.api;

import com.numix.core.auth.entity.AppUser;
import com.numix.core.auth.entity.RoleCode;
import com.numix.core.auth.service.AppUserService;
import com.numix.core.auth.service.exception.AuthBusinessException;
import com.numix.core.auth.service.model.CreateAccountUserRequest;
import com.numix.core.auth.service.model.UpdateAccountUserRequest;
import com.numix.web.controller.ApiActionResponse;
import com.numix.web.controller.UsersPageApiResponse;
import com.numix.web.controller.form.CreateUserForm;
import com.numix.web.controller.form.UpdateUserForm;
import com.numix.web.security.CurrentUserResolver;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountUsersApiController {

    private final AppUserService appUserService;
    private final CurrentUserResolver currentUserResolver;

    public AccountUsersApiController(AppUserService appUserService, CurrentUserResolver currentUserResolver) {
        this.appUserService = appUserService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/account/users/api/data")
    public UsersPageApiResponse usersPageData(Authentication authentication) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);
        return buildUsersPageData(currentUser);
    }

    @PostMapping("/account/users/api/create")
    public ApiActionResponse createUserApi(
        Authentication authentication,
        @RequestBody CreateUserForm createForm
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
            return new ApiActionResponse(true, "Usuario creado correctamente", buildUsersPageData(currentUser));
        } catch (AuthBusinessException ex) {
            return new ApiActionResponse(false, ex.getMessage(), buildUsersPageData(currentUser));
        }
    }

    @PostMapping("/account/users/api/update")
    public ApiActionResponse updateUserApi(
        Authentication authentication,
        @RequestBody UpdateUserForm updateForm
    ) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);
        try {
            appUserService.updateAccountUser(new UpdateAccountUserRequest(
                currentUser.getId(),
                updateForm.getTargetUserId(),
                updateForm.getRoleCode(),
                updateForm.getEnabled()
            ));
            return new ApiActionResponse(true, "Usuario actualizado correctamente", buildUsersPageData(currentUser));
        } catch (AuthBusinessException ex) {
            return new ApiActionResponse(false, ex.getMessage(), buildUsersPageData(currentUser));
        }
    }

    private UsersPageApiResponse buildUsersPageData(AppUser currentUser) {
        return new UsersPageApiResponse(
            appUserService.listAccountUsers(currentUser.getId()),
            manageableAssignableRoles()
        );
    }

    private List<RoleCode> manageableAssignableRoles() {
        return List.of(RoleCode.ADMIN, RoleCode.AUX);
    }
}
