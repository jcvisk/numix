package com.numix.web.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class AuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {
        if (exception instanceof LockedException) {
            setDefaultFailureUrl("/login?error=account-suspended");
        } else if (exception instanceof DisabledException) {
            setDefaultFailureUrl("/login?error=user-disabled");
        } else {
            setDefaultFailureUrl("/login?error=invalid");
        }
        super.onAuthenticationFailure(request, response, exception);
    }
}
