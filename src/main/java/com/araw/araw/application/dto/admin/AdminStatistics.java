package com.araw.araw.application.dto.admin;

import com.araw.araw.domain.admin.valueobject.AdminRole;

import java.util.Map;

@lombok.Builder
@lombok.Getter
public class AdminStatistics {
    private Long totalAdmins;
    private Long activeAdmins;
    private Long inactiveAdmins;
    private Long verifiedAdmins;
    private Long unverifiedAdmins;
    private Long lockedAccounts;
    private Long twoFactorEnabledCount;
    private Map<AdminRole, Long> adminsByRole;
    private Long departmentCount;
    private Long recentlyActive;
}
