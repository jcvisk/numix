package com.numix.web.controller.form;

import java.util.Set;

public class AssignUserCompaniesForm {
    private Long targetUserId;
    private Set<Long> companyIds;

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public Set<Long> getCompanyIds() {
        return companyIds;
    }

    public void setCompanyIds(Set<Long> companyIds) {
        this.companyIds = companyIds;
    }
}
