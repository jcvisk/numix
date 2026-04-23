package com.numix.web.controller.form;

import com.numix.core.company.entity.CompanyStatus;
import java.time.LocalDate;

public class UpdateCompanyForm {
    private Long companyId;
    private String name;
    private String legalName;
    private String taxId;
    private String fiscalRegime;
    private String taxZipCode;
    private Long baseCurrencyId;
    private String email;
    private String phone;
    private String address;
    private String curp;
    private LocalDate birthDate;
    private CompanyStatus status;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getFiscalRegime() {
        return fiscalRegime;
    }

    public void setFiscalRegime(String fiscalRegime) {
        this.fiscalRegime = fiscalRegime;
    }

    public String getTaxZipCode() {
        return taxZipCode;
    }

    public void setTaxZipCode(String taxZipCode) {
        this.taxZipCode = taxZipCode;
    }

    public Long getBaseCurrencyId() {
        return baseCurrencyId;
    }

    public void setBaseCurrencyId(Long baseCurrencyId) {
        this.baseCurrencyId = baseCurrencyId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        this.curp = curp;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public CompanyStatus getStatus() {
        return status;
    }

    public void setStatus(CompanyStatus status) {
        this.status = status;
    }
}
