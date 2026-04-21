package com.numix.core.auth.service;

import com.numix.core.auth.entity.Account;
import com.numix.core.auth.entity.AppStatus;
import com.numix.core.auth.entity.AppUser;
import com.numix.core.auth.entity.Role;
import com.numix.core.auth.entity.RoleCode;
import com.numix.core.auth.entity.StatusCode;
import com.numix.core.auth.repository.AccountRepository;
import com.numix.core.auth.repository.AppStatusRepository;
import com.numix.core.auth.repository.AppUserRepository;
import com.numix.core.auth.repository.RoleRepository;
import com.numix.core.auth.service.exception.AuthBusinessException;
import com.numix.core.auth.service.model.ChangeCredentialsRequest;
import com.numix.core.auth.service.model.CreateAccountUserRequest;
import com.numix.core.auth.service.model.PublicRegistrationRequest;
import com.numix.core.auth.service.model.UpdateAccountUserRequest;
import com.numix.core.auth.service.model.UserSummary;
import com.numix.core.company.entity.Company;
import com.numix.core.company.entity.UserCompany;
import com.numix.core.company.repository.CompanyRepository;
import com.numix.core.company.repository.UserCompanyRepository;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserService {

    private static final Set<RoleCode> MANAGED_ACCOUNT_ROLES = EnumSet.of(RoleCode.OWNER, RoleCode.ADMIN, RoleCode.AUX);
    private static final Set<RoleCode> ADMINISTRATIVE_ROLES = EnumSet.of(RoleCode.OWNER, RoleCode.ADMIN);

    private final AppUserRepository appUserRepository;
    private final AccountRepository accountRepository;
    private final AppStatusRepository appStatusRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(
        AppUserRepository appUserRepository,
        AccountRepository accountRepository,
        AppStatusRepository appStatusRepository,
        RoleRepository roleRepository,
        CompanyRepository companyRepository,
        UserCompanyRepository userCompanyRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.accountRepository = accountRepository;
        this.appStatusRepository = appStatusRepository;
        this.roleRepository = roleRepository;
        this.companyRepository = companyRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return appUserRepository.findByEmailIgnoreCase(normalizeEmail(email));
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findById(Long id) {
        return appUserRepository.findById(id);
    }

    @Transactional
    public AppUser registerPublicOwner(PublicRegistrationRequest request) {
        String email = normalizeEmail(request.email());
        validateNewUserEmail(email);

        Account account = new Account();
        account.setName(requireText(request.accountName(), "El nombre de la cuenta es obligatorio"));
        account.setStatus(getStatusOrFail(StatusCode.ACTIVE));
        account.setCreatedAt(OffsetDateTime.now());
        account.setUpdatedAt(OffsetDateTime.now());
        Account persistedAccount = accountRepository.save(account);

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setFullName(requireText(request.fullName(), "El nombre completo es obligatorio"));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setAccount(persistedAccount);
        user.setRoles(singleRoleSet(RoleCode.OWNER));
        return appUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserSummary> listAccountUsers(Long actorUserId) {
        AppUser actor = getUserOrFail(actorUserId);
        ensureCanManageAccountUsers(actor);
        Long accountId = requireAccountId(actor);

        return appUserRepository.findAllByAccountIdOrderByIdAsc(accountId)
            .stream()
            .map(this::toSummary)
            .toList();
    }

    @Transactional
    public AppUser createAccountUser(CreateAccountUserRequest request) {
        AppUser actor = getUserOrFail(request.actorUserId());
        ensureCanManageAccountUsers(actor);

        RoleCode targetRoleCode = requireManagedRole(request.roleCode());
        if (targetRoleCode == RoleCode.OWNER || targetRoleCode == RoleCode.SUPER_ADMIN) {
            throw new AuthBusinessException("No se puede asignar este rol desde la gestión de cuenta");
        }

        String email = normalizeEmail(request.email());
        validateNewUserEmail(email);

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setFullName(requireText(request.fullName(), "El nombre completo es obligatorio"));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setAccount(actor.getAccount());
        user.setRoles(singleRoleSet(targetRoleCode));
        AppUser persisted = appUserRepository.save(user);
        upsertCompanyAssignments(actor, persisted, targetRoleCode, request.companyIds());
        return persisted;
    }

    @Transactional
    public AppUser updateAccountUser(UpdateAccountUserRequest request) {
        AppUser actor = getUserOrFail(request.actorUserId());
        ensureCanManageAccountUsers(actor);
        Long accountId = requireAccountId(actor);

        AppUser target = appUserRepository.findByIdAndAccountId(request.targetUserId(), accountId)
            .orElseThrow(() -> new AuthBusinessException("Usuario objetivo no encontrado para esta cuenta"));

        if (target.getId().equals(actor.getId())) {
            throw new AuthBusinessException("No puedes cambiar tu propio rol o estado desde esta pantalla");
        }

        if (request.roleCode() != null) {
            RoleCode requestedRole = requireManagedRole(request.roleCode());
            if (requestedRole == RoleCode.OWNER || requestedRole == RoleCode.SUPER_ADMIN) {
                throw new AuthBusinessException("No se puede asignar este rol desde la gestión de cuenta");
            }
            target.setRoles(singleRoleSet(requestedRole));
        }

        if (request.enabled() != null) {
            target.setEnabled(request.enabled());
        }

        AppUser persisted = appUserRepository.save(target);
        RoleCode targetRole = request.roleCode() != null ? request.roleCode() : primaryRole(target);
        upsertCompanyAssignments(actor, persisted, targetRole, request.companyIds());
        return persisted;
    }

    @Transactional
    public AppUser changeCredentials(ChangeCredentialsRequest request) {
        AppUser user = getUserOrFail(request.userId());
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new AuthBusinessException("La contraseña actual no es correcta");
        }

        String normalizedEmail = normalizeEmail(request.newEmail());
        if (!user.getEmail().equalsIgnoreCase(normalizedEmail)) {
            validateNewUserEmail(normalizedEmail);
            user.setEmail(normalizedEmail);
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        return appUserRepository.save(user);
    }

    @Transactional
    public void updateAccountStatus(Long actorUserId, StatusCode statusCode) {
        AppUser actor = getUserOrFail(actorUserId);
        if (primaryRole(actor) != RoleCode.SUPER_ADMIN) {
            throw new AuthBusinessException("Solo SUPER_ADMIN puede cambiar el estado de la cuenta");
        }
        if (actor.getAccount() == null) {
            throw new AuthBusinessException("SUPER_ADMIN no tiene cuenta asociada para esta operación");
        }

        Account account = actor.getAccount();
        account.setStatus(getStatusOrFail(statusCode));
        account.setUpdatedAt(OffsetDateTime.now());
        accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<RoleCode> manageableAccountRoles() {
        return List.of(RoleCode.OWNER, RoleCode.ADMIN, RoleCode.AUX);
    }

    private void ensureCanManageAccountUsers(AppUser actor) {
        if (!ADMINISTRATIVE_ROLES.contains(primaryRole(actor))) {
            throw new AuthBusinessException("No tienes permisos para gestionar usuarios");
        }
        if (actor.getAccount() == null || actor.getAccount().getStatus() == null
            || actor.getAccount().getStatus().getCode() != StatusCode.ACTIVE) {
            throw new AuthBusinessException("La cuenta no está activa");
        }
    }

    private Long requireAccountId(AppUser user) {
        if (user.getAccount() == null) {
            throw new AuthBusinessException("El usuario no tiene una cuenta asociada");
        }
        return user.getAccount().getId();
    }

    private AppUser getUserOrFail(Long id) {
        return appUserRepository.findById(id)
            .orElseThrow(() -> new AuthBusinessException("Usuario no encontrado"));
    }

    private void validateNewUserEmail(String email) {
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new AuthBusinessException("Ya existe una cuenta con ese correo");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new AuthBusinessException("El correo es obligatorio");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new AuthBusinessException("El correo es obligatorio");
        }
        return normalized;
    }

    private String requireText(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new AuthBusinessException(errorMessage);
        }
        return value.trim();
    }

    private Role getRoleOrFail(RoleCode roleCode) {
        return roleRepository.findByCode(roleCode)
            .orElseThrow(() -> new AuthBusinessException("No existe el rol requerido: " + roleCode));
    }

    private AppStatus getStatusOrFail(StatusCode statusCode) {
        if (statusCode == null) {
            throw new AuthBusinessException("Estatus inválido");
        }
        return appStatusRepository.findByCode(statusCode)
            .orElseThrow(() -> new AuthBusinessException("No existe el estatus requerido: " + statusCode));
    }

    private Set<Role> singleRoleSet(RoleCode roleCode) {
        Role role = getRoleOrFail(roleCode);
        Set<Role> roles = new LinkedHashSet<>();
        roles.add(role);
        return roles;
    }

    public RoleCode primaryRole(AppUser user) {
        if (user.getRoles().isEmpty()) {
            throw new AuthBusinessException("El usuario no tiene rol asignado");
        }
        if (user.getRoles().size() > 1) {
            throw new AuthBusinessException("El usuario tiene más de un rol y no es válido en la versión actual");
        }
        return user.getRoles().iterator().next().getCode();
    }

    private RoleCode requireManagedRole(RoleCode roleCode) {
        if (roleCode == null || !MANAGED_ACCOUNT_ROLES.contains(roleCode)) {
            throw new AuthBusinessException("Rol inválido para operación de cuenta");
        }
        return roleCode;
    }

    private void upsertCompanyAssignments(AppUser actor, AppUser target, RoleCode targetRole, Set<Long> companyIds) {
        if (companyIds == null) {
            return;
        }
        if (primaryRole(actor) != RoleCode.OWNER) {
            throw new AuthBusinessException("Solo OWNER puede asignar empresas a usuarios");
        }
        if (targetRole != RoleCode.ADMIN && targetRole != RoleCode.AUX) {
            throw new AuthBusinessException("Solo se pueden asignar empresas a usuarios ADMIN o AUX");
        }
        if (companyIds.isEmpty()) {
            throw new AuthBusinessException("Debes asignar al menos una empresa");
        }

        Long accountId = requireAccountId(actor);
        List<Company> companies = companyRepository.findAllById(companyIds)
            .stream()
            .filter(company -> company.getAccount().getId().equals(accountId))
            .toList();

        if (companies.size() != companyIds.size()) {
            throw new AuthBusinessException("Una o más empresas no pertenecen a la cuenta");
        }

        userCompanyRepository.deleteAllByUser_Id(target.getId());
        List<UserCompany> assignments = companies.stream()
            .map(company -> new UserCompany(target, company, actor))
            .toList();
        userCompanyRepository.saveAll(assignments);
    }

    private UserSummary toSummary(AppUser user) {
        return new UserSummary(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            primaryRole(user),
            user.isEnabled()
        );
    }

}
