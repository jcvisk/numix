package com.numix.core.company.service;

import com.numix.core.auth.entity.AppUser;
import com.numix.core.auth.entity.RoleCode;
import com.numix.core.auth.entity.StatusCode;
import com.numix.core.auth.repository.AppUserRepository;
import com.numix.core.auth.service.AppUserService;
import com.numix.core.auth.service.exception.AuthBusinessException;
import com.numix.core.company.entity.Company;
import com.numix.core.company.entity.CompanyStatus;
import com.numix.core.company.entity.Currency;
import com.numix.core.company.entity.UserCompany;
import com.numix.core.company.repository.CompanyRepository;
import com.numix.core.company.repository.CurrencyRepository;
import com.numix.core.company.repository.UserCompanyRepository;
import com.numix.core.company.service.model.AssignUserCompaniesRequest;
import com.numix.core.company.service.model.CompanySummary;
import com.numix.core.company.service.model.CreateCompanyRequest;
import com.numix.core.company.service.model.CurrencySummary;
import com.numix.core.company.service.model.DeleteCompanyRequest;
import com.numix.core.company.service.model.UpdateCompanyRequest;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private final AppUserRepository appUserRepository;
    private final AppUserService appUserService;
    private final CompanyRepository companyRepository;
    private final CurrencyRepository currencyRepository;
    private final UserCompanyRepository userCompanyRepository;

    public CompanyService(
        AppUserRepository appUserRepository,
        AppUserService appUserService,
        CompanyRepository companyRepository,
        CurrencyRepository currencyRepository,
        UserCompanyRepository userCompanyRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.appUserService = appUserService;
        this.companyRepository = companyRepository;
        this.currencyRepository = currencyRepository;
        this.userCompanyRepository = userCompanyRepository;
    }

    @Transactional(readOnly = true)
    public List<CompanySummary> listVisibleCompanies(Long actorUserId) {
        AppUser actor = getUserOrFail(actorUserId);
        Long accountId = requireActiveAccountId(actor);

        RoleCode actorRole = appUserService.primaryRole(actor);
        if (actorRole == RoleCode.OWNER || actorRole == RoleCode.SUPER_ADMIN) {
            return companyRepository.findAllByAccountIdOrderByIdAsc(accountId)
                .stream()
                .map(this::toCompanySummary)
                .toList();
        }

        return userCompanyRepository.findAllByUser_IdAndCompany_Account_IdOrderByCompany_IdAsc(actor.getId(), accountId)
            .stream()
            .map(UserCompany::getCompany)
            .map(this::toCompanySummary)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CurrencySummary> listCurrencies() {
        return currencyRepository.findAllByOrderByCodeAsc()
            .stream()
            .map(this::toCurrencySummary)
            .toList();
    }

    @Transactional
    public CompanySummary createCompany(CreateCompanyRequest request) {
        AppUser actor = getUserOrFail(request.actorUserId());
        ensureOwner(actor);
        Long accountId = requireActiveAccountId(actor);

        String taxId = normalizeTaxId(request.taxId());
        validateTaxIdAvailable(taxId);

        Currency currency = getCurrencyOrFail(request.baseCurrencyId());
        OffsetDateTime now = OffsetDateTime.now();

        Company company = new Company();
        company.setAccount(actor.getAccount());
        company.setName(requireText(request.name(), "El nombre de la empresa es obligatorio"));
        company.setLegalName(requireText(request.legalName(), "La razón social es obligatoria"));
        company.setTaxId(taxId);
        company.setFiscalRegime(requireText(request.fiscalRegime(), "El régimen fiscal es obligatorio"));
        company.setTaxZipCode(trimToNull(request.taxZipCode()));
        company.setBaseCurrency(currency);
        company.setEmail(trimToNull(request.email()));
        company.setPhone(trimToNull(request.phone()));
        company.setAddress(trimToNull(request.address()));
        company.setStatus(CompanyStatus.ACTIVE);
        company.setCreatedAt(now);
        company.setUpdatedAt(now);

        Company persisted = companyRepository.save(company);
        if (!persisted.getAccount().getId().equals(accountId)) {
            throw new AuthBusinessException("No se pudo crear la empresa en la cuenta esperada");
        }

        return toCompanySummary(persisted);
    }

    @Transactional
    public CompanySummary updateCompany(UpdateCompanyRequest request) {
        AppUser actor = getUserOrFail(request.actorUserId());
        ensureOwner(actor);
        Long accountId = requireActiveAccountId(actor);

        Company company = companyRepository.findByIdAndAccountId(request.companyId(), accountId)
            .orElseThrow(() -> new AuthBusinessException("Empresa no encontrada para esta cuenta"));

        String newTaxId = normalizeTaxId(request.taxId());
        if (!company.getTaxId().equalsIgnoreCase(newTaxId)) {
            validateTaxIdAvailable(newTaxId);
            company.setTaxId(newTaxId);
        }

        company.setName(requireText(request.name(), "El nombre de la empresa es obligatorio"));
        company.setLegalName(requireText(request.legalName(), "La razón social es obligatoria"));
        company.setFiscalRegime(requireText(request.fiscalRegime(), "El régimen fiscal es obligatorio"));
        company.setTaxZipCode(trimToNull(request.taxZipCode()));
        company.setBaseCurrency(getCurrencyOrFail(request.baseCurrencyId()));
        company.setEmail(trimToNull(request.email()));
        company.setPhone(trimToNull(request.phone()));
        company.setAddress(trimToNull(request.address()));
        company.setStatus(request.status() == null ? CompanyStatus.ACTIVE : request.status());
        company.setUpdatedAt(OffsetDateTime.now());

        return toCompanySummary(companyRepository.save(company));
    }

    @Transactional
    public void deleteCompany(DeleteCompanyRequest request) {
        AppUser actor = getUserOrFail(request.actorUserId());
        ensureOwner(actor);
        Long accountId = requireActiveAccountId(actor);

        Company company = companyRepository.findByIdAndAccountId(request.companyId(), accountId)
            .orElseThrow(() -> new AuthBusinessException("Empresa no encontrada para esta cuenta"));

        userCompanyRepository.deleteAllByCompany_Id(company.getId());
        companyRepository.delete(company);
    }

    @Transactional
    public void assignCompaniesToUser(AssignUserCompaniesRequest request) {
        AppUser actor = getUserOrFail(request.actorUserId());
        ensureOwner(actor);
        Long accountId = requireActiveAccountId(actor);

        AppUser targetUser = appUserRepository.findByIdAndAccountId(request.targetUserId(), accountId)
            .orElseThrow(() -> new AuthBusinessException("Usuario objetivo no encontrado para esta cuenta"));

        RoleCode targetRole = appUserService.primaryRole(targetUser);
        if (targetRole != RoleCode.ADMIN && targetRole != RoleCode.AUX) {
            throw new AuthBusinessException("Solo se pueden asignar empresas a usuarios ADMIN o AUX");
        }

        Set<Long> companyIds = normalizeCompanyIds(request.companyIds());
        List<Company> companies = companyRepository.findAllById(companyIds)
            .stream()
            .filter(company -> company.getAccount().getId().equals(accountId))
            .toList();

        if (companies.size() != companyIds.size()) {
            throw new AuthBusinessException("Una o más empresas no pertenecen a la cuenta del OWNER");
        }

        userCompanyRepository.deleteAllByUser_Id(targetUser.getId());
        List<UserCompany> assignments = companies.stream()
            .map(company -> new UserCompany(targetUser, company, actor))
            .toList();
        userCompanyRepository.saveAll(assignments);
    }

    private Long requireActiveAccountId(AppUser user) {
        if (user.getAccount() == null) {
            throw new AuthBusinessException("El usuario no tiene una cuenta asociada");
        }
        if (user.getAccount().getStatus() == null || user.getAccount().getStatus().getCode() != StatusCode.ACTIVE) {
            throw new AuthBusinessException("La cuenta no está activa");
        }
        return user.getAccount().getId();
    }

    private void ensureOwner(AppUser actor) {
        RoleCode roleCode = appUserService.primaryRole(actor);
        if (roleCode != RoleCode.OWNER) {
            throw new AuthBusinessException("Solo OWNER puede gestionar empresas");
        }
    }

    private AppUser getUserOrFail(Long userId) {
        return appUserRepository.findById(userId)
            .orElseThrow(() -> new AuthBusinessException("Usuario no encontrado"));
    }

    private Currency getCurrencyOrFail(Long currencyId) {
        if (currencyId == null) {
            throw new AuthBusinessException("La moneda base es obligatoria");
        }
        return currencyRepository.findById(currencyId)
            .orElseThrow(() -> new AuthBusinessException("Moneda base no encontrada"));
    }

    private void validateTaxIdAvailable(String taxId) {
        if (companyRepository.existsByTaxIdIgnoreCase(taxId)) {
            throw new AuthBusinessException("Ya existe una empresa con ese RFC/Tax ID");
        }
    }

    private Set<Long> normalizeCompanyIds(Set<Long> companyIds) {
        if (companyIds == null || companyIds.isEmpty()) {
            throw new AuthBusinessException("Debes asignar al menos una empresa");
        }
        Set<Long> normalized = new LinkedHashSet<>();
        for (Long id : companyIds) {
            if (id == null || id <= 0) {
                throw new AuthBusinessException("ID de empresa inválido");
            }
            normalized.add(id);
        }
        return normalized;
    }

    private String requireText(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new AuthBusinessException(errorMessage);
        }
        return value.trim();
    }

    private String normalizeTaxId(String taxId) {
        String normalized = requireText(taxId, "El RFC/Tax ID es obligatorio").toUpperCase(Locale.ROOT);
        if (normalized.length() > 50) {
            throw new AuthBusinessException("El RFC/Tax ID no puede exceder 50 caracteres");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CompanySummary toCompanySummary(Company company) {
        return new CompanySummary(
            company.getId(),
            company.getName(),
            company.getLegalName(),
            company.getTaxId(),
            company.getFiscalRegime(),
            company.getTaxZipCode(),
            company.getBaseCurrency().getId(),
            company.getBaseCurrency().getCode(),
            company.getEmail(),
            company.getPhone(),
            company.getAddress(),
            company.getStatus()
        );
    }

    private CurrencySummary toCurrencySummary(Currency currency) {
        return new CurrencySummary(
            currency.getId(),
            currency.getCode(),
            currency.getName(),
            currency.getSymbol()
        );
    }
}
