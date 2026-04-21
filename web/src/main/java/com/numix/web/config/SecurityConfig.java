package com.numix.web.config;

import com.numix.core.auth.entity.AppUser;
import com.numix.core.auth.entity.StatusCode;
import com.numix.core.auth.service.AppUserService;
import com.numix.web.security.AccountStateEnforcementFilter;
import com.numix.web.security.AuthFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        AuthFailureHandler authFailureHandler,
        AccountStateEnforcementFilter accountStateEnforcementFilter
    ) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login",
                    "/register",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/fonts/**",
                    "/pdf/**",
                    "/bootstrap/**",
                    "/plugins/**",
                    "/documentation/**",
                    "/favicon.ico",
                    "/error"
                ).permitAll()
                .requestMatchers("/account/users/**").hasAnyAuthority("OWNER", "ADMIN", "SUPER_ADMIN")
                .requestMatchers("/account/companies/**").hasAuthority("OWNER")
                .requestMatchers("/dashboard/**", "/profile/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureHandler(authFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
            )
            .rememberMe(Customizer.withDefaults())
            .addFilterAfter(accountStateEnforcementFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(AppUserService appUserService) {
        return email -> appUserService
            .findByEmail(email)
            .map(this::toUserDetails)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private UserDetails toUserDetails(AppUser user) {
        boolean accountIsSuspended = user.getAccount() != null
            && user.getAccount().getStatus() != null
            && user.getAccount().getStatus().getCode() == StatusCode.SUSPENDED;
        return new User(
            user.getEmail(),
            user.getPasswordHash(),
            user.isEnabled(),
            true,
            true,
            !accountIsSuspended,
            user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getCode().name()))
                .toList()
        );
    }
}
