package com.numix.web.controller.api;

import com.numix.web.controller.DashboardActivity;
import com.numix.web.controller.DashboardApiSummaryResponse;
import com.numix.web.controller.DashboardCard;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardApiController {

    @GetMapping("/dashboard/api/summary")
    public DashboardApiSummaryResponse summary(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "Usuario";
        return new DashboardApiSummaryResponse(
            "Bienvenido, " + username,
            OffsetDateTime.now().toString(),
            List.of(
                new DashboardCard("Usuarios activos", "24", "bi bi-people fs-3 text-info"),
                new DashboardCard("Usuarios inactivos", "3", "bi bi-person-x fs-3 text-warning"),
                new DashboardCard("Accesos hoy", "128", "bi bi-box-arrow-in-right fs-3 text-success"),
                new DashboardCard("Alertas de seguridad", "1", "bi bi-shield-exclamation fs-3 text-danger")
            ),
            List.of(
                new DashboardActivity("Nuevos usuarios", "2"),
                new DashboardActivity("Cambios de credenciales", "5"),
                new DashboardActivity("Intentos fallidos de login", "1")
            )
        );
    }
}
