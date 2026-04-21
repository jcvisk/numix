package com.numix.web.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountCompaniesController extends BaseViewController {

    public AccountCompaniesController() {
        super("empresas");
    }

    @GetMapping("/account/companies")
    public String companiesPage() {
        return "account/companies";
    }
}
