package com.numix.web.security;

import com.numix.core.auth.entity.AppUser;
import com.numix.core.auth.entity.RoleCode;
import com.numix.core.auth.entity.StatusCode;
import com.numix.core.auth.service.AppUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AccountStateEnforcementFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_PREFIXES = Set.of(
        "/login",
        "/register",
        "/css/",
        "/js/",
        "/img/",
        "/fonts/",
        "/pdf/",
        "/bootstrap/",
        "/plugins/",
        "/documentation/",
        "/favicon.ico",
        "/error"
    );

    private final AppUserService appUserService;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    public AccountStateEnforcementFilter(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        AppUser user = appUserService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            forceLogout(request, response, "invalid");
            return;
        }
        if (!user.isEnabled()) {
            forceLogout(request, response, "user-disabled");
            return;
        }
        if (!isSuperAdmin(user)
            && user.getAccount() != null
            && user.getAccount().getStatus() != null
            && user.getAccount().getStatus().getCode() == StatusCode.SUSPENDED) {
            forceLogout(request, response, "account-suspended");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSuperAdmin(AppUser user) {
        return user.getRoles().stream().anyMatch(role -> role.getCode() == RoleCode.SUPER_ADMIN);
    }

    private void forceLogout(HttpServletRequest request, HttpServletResponse response, String error) throws IOException {
        logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        response.sendRedirect("/login?error=" + error);
    }
}
