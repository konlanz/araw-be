package com.araw.araw.application.dto.admin;

import com.araw.araw.domain.admin.valueobject.AdminRole;

import java.util.Map;

@lombok.Builder
@lombok.Getter
public class DepartmentStats {
    private String departmentName;
    private Long totalAdmins;
    private Long activeAdmins;
    private Long verifiedAdmins;
    private Map<AdminRole, Long> roleDistribution;
}
