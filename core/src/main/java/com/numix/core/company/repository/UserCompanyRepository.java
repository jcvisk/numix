package com.numix.core.company.repository;

import com.numix.core.company.entity.UserCompany;
import com.numix.core.company.entity.UserCompanyId;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCompanyRepository extends JpaRepository<UserCompany, UserCompanyId> {

    @EntityGraph(attributePaths = {"company", "company.baseCurrency", "company.account"})
    List<UserCompany> findAllByUser_IdAndCompany_Account_IdOrderByCompany_IdAsc(Long userId, Long accountId);

    void deleteAllByUser_Id(Long userId);

    void deleteAllByCompany_Id(Long companyId);
}
