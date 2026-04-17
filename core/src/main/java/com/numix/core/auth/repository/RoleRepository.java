package com.numix.core.auth.repository;

import com.numix.core.auth.entity.Role;
import com.numix.core.auth.entity.RoleCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(RoleCode code);
}
