package com.numix.web.controller.api;

import com.numix.core.auth.entity.AppUser;
import com.numix.core.auth.service.exception.AuthBusinessException;
import com.numix.core.company.service.CompanyService;
import com.numix.core.company.service.model.AssignUserCompaniesRequest;
import com.numix.core.company.service.model.CreateCompanyRequest;
import com.numix.core.company.service.model.DeleteCompanyRequest;
import com.numix.core.company.service.model.UpdateCompanyRequest;
import com.numix.web.controller.CompaniesApiActionResponse;
import com.numix.web.controller.CompaniesPageApiResponse;
import com.numix.web.controller.form.AssignUserCompaniesForm;
import com.numix.web.controller.form.CreateCompanyForm;
import com.numix.web.controller.form.DeleteCompanyForm;
import com.numix.web.controller.form.UpdateCompanyForm;
import com.numix.web.security.CurrentUserResolver;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountCompaniesApiController {

    private final CompanyService companyService;
    private final CurrentUserResolver currentUserResolver;

    public AccountCompaniesApiController(CompanyService companyService, CurrentUserResolver currentUserResolver) {
        this.companyService = companyService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/account/companies/api/data")
    public CompaniesPageApiResponse companiesPageData(Authentication authentication) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);
        return buildCompaniesPageData(currentUser);
    }

    @PostMapping("/account/companies/api/create")
    public CompaniesApiActionResponse createCompany(
        Authentication authentication,
        @RequestBody CreateCompanyForm createForm
    ) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);
        try {
            companyService.createCompany(new CreateCompanyRequest(
                currentUser.getId(),
                createForm.getName(),
                createForm.getLegalName(),
                createForm.getTaxId(),
                createForm.getFiscalRegime(),
                createForm.getTaxZipCode(),
                createForm.getBaseCurrencyId(),
                createForm.getEmail(),
                createForm.getPhone(),
                createForm.getAddress()
            ));
            return new CompaniesApiActionResponse(true, "Empresa creada correctamente", buildCompaniesPageData(currentUser));
        } catch (AuthBusinessException ex) {
            return new CompaniesApiActionResponse(false, ex.getMessage(), buildCompaniesPageData(currentUser));
        }
    }

    @PostMapping("/account/companies/api/update")
    public CompaniesApiActionResponse updateCompany(
        Authentication authentication,
        @RequestBody UpdateCompanyForm updateForm
    ) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);
        try {
            companyService.updateCompany(new UpdateCompanyRequest(
                currentUser.getId(),
                updateForm.getCompanyId(),
                updateForm.getName(),
                updateForm.getLegalName(),
                updateForm.getTaxId(),
                updateForm.getFiscalRegime(),
                updateForm.getTaxZipCode(),
                updateForm.getBaseCurrencyId(),
                updateForm.getEmail(),
                updateForm.getPhone(),
                updateForm.getAddress(),
                updateForm.getStatus()
            ));
            return new CompaniesApiActionResponse(true, "Empresa actualizada correctamente", buildCompaniesPageData(currentUser));
        } catch (AuthBusinessException ex) {
            return new CompaniesApiActionResponse(false, ex.getMessage(), buildCompaniesPageData(currentUser));
        }
    }

    @PostMapping("/account/companies/api/delete")
    public CompaniesApiActionResponse deleteCompany(
        Authentication authentication,
        @RequestBody DeleteCompanyForm deleteForm
    ) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);
        try {
            companyService.deleteCompany(new DeleteCompanyRequest(
                currentUser.getId(),
                deleteForm.getCompanyId()
            ));
            return new CompaniesApiActionResponse(true, "Empresa eliminada correctamente", buildCompaniesPageData(currentUser));
        } catch (AuthBusinessException ex) {
            return new CompaniesApiActionResponse(false, ex.getMessage(), buildCompaniesPageData(currentUser));
        }
    }

    @PostMapping("/account/companies/api/assign-user")
    public CompaniesApiActionResponse assignCompaniesToUser(
        Authentication authentication,
        @RequestBody AssignUserCompaniesForm assignForm
    ) {
        AppUser currentUser = currentUserResolver.resolveOrFail(authentication);
        try {
            companyService.assignCompaniesToUser(new AssignUserCompaniesRequest(
                currentUser.getId(),
                assignForm.getTargetUserId(),
                assignForm.getCompanyIds()
            ));
            return new CompaniesApiActionResponse(true, "Empresas asignadas correctamente", buildCompaniesPageData(currentUser));
        } catch (AuthBusinessException ex) {
            return new CompaniesApiActionResponse(false, ex.getMessage(), buildCompaniesPageData(currentUser));
        }
    }

    private CompaniesPageApiResponse buildCompaniesPageData(AppUser currentUser) {
        return new CompaniesPageApiResponse(
            companyService.listVisibleCompanies(currentUser.getId()),
            companyService.listCurrencies()
        );
    }
}
