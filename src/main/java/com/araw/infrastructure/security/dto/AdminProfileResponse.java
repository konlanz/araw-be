package com.araw.infrastructure.security.dto;

import com.araw.araw.domain.admin.valueobject.AdminPermission;
import com.araw.araw.domain.admin.valueobject.AdminRole;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO returned by GET /api/auth/me after successful Google OAuth2 authentication.
 */
public record AdminProfileResponse(
        UUID id,
        String email,
        String username,
        String firstName,
        String lastName,
        String fullName,
        String title,
        String department,
        String profilePictureUrl,
        AdminRole role,
        String roleDisplayName,
        Set<AdminPermission> permissions,
        LocalDateTime lastLoginAt
) {
}
