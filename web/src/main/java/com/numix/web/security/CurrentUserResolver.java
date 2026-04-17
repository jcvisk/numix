package com.numix.web.security;

import com.numix.core.auth.entity.AppUser;
import com.numix.core.auth.service.AppUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    private final AppUserService appUserService;

    public CurrentUserResolver(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    public AppUser resolveOrFail(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("No hay autenticación en contexto");
        }
        return appUserService.findByEmail(authentication.getName())
            .orElseThrow(() -> new IllegalStateException("No fue posible resolver al usuario autenticado"));
    }
}
