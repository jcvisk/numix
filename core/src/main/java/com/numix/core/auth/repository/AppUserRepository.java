package com.numix.core.auth.repository;

import com.numix.core.auth.entity.AppUser;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    @EntityGraph(attributePaths = {"roles", "account"})
    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"roles", "account"})
    List<AppUser> findAllByAccountIdOrderByIdAsc(Long accountId);

    @EntityGraph(attributePaths = {"roles", "account"})
    Optional<AppUser> findByIdAndAccountId(Long id, Long accountId);
}
