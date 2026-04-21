package com.numix.core.company.repository;

import com.numix.core.company.entity.Currency;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    List<Currency> findAllByOrderByCodeAsc();
}
