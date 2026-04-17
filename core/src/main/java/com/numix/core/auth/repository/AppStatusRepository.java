package com.numix.core.auth.repository;

import com.numix.core.auth.entity.AppStatus;
import com.numix.core.auth.entity.StatusCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppStatusRepository extends JpaRepository<AppStatus, Long> {

    Optional<AppStatus> findByCode(StatusCode code);
}
