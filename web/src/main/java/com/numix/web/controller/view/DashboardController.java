package com.numix.web.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController extends BaseViewController {

    public DashboardController() {
        super("dashboard");
    }

    @GetMapping("/dashboard")
    public String dashboard(){
        return "dashboard/dashboard";
    }
}
