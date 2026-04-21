package com.numix.web.controller;

import com.numix.core.auth.entity.RoleCode;
import com.numix.core.auth.service.model.UserSummary;
import java.util.List;

public record UsersPageApiResponse(
    List<UserSummary> users,
    List<RoleCode> roles
) {
}
