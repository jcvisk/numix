package com.numix.core.company.repository;

import com.numix.core.company.entity.Company;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    @EntityGraph(attributePaths = {"baseCurrency", "account"})
    List<Company> findAllByAccountIdOrderByIdAsc(Long accountId);

    @EntityGraph(attributePaths = {"baseCurrency", "account"})
    Optional<Company> findByIdAndAccountId(Long id, Long accountId);

    boolean existsByTaxIdIgnoreCase(String taxId);
}
