package com.araw.araw.domain.admin.valueobject;

import lombok.Getter;

import java.util.Set;
import java.util.HashSet;

public enum AdminRole {
    SUPER_ADMIN(1, "Super Admin",
            "Full system access, can manage other admins",
            Set.of(AdminPermission.values())),

    ADMIN(2, "Administrator",
            "Can manage events, applications, and participants",
            Set.of(
                    AdminPermission.MANAGE_EVENTS,
                    AdminPermission.REVIEW_APPLICATIONS,
                    AdminPermission.MANAGE_PARTICIPANTS,
                    AdminPermission.MANAGE_FEEDBACK,
                    AdminPermission.VIEW_REPORTS,
                    AdminPermission.EXPORT_DATA
            )),

    EVENT_MANAGER(3, "Event Manager",
            "Can create and manage events",
            Set.of(
                    AdminPermission.MANAGE_EVENTS,
                    AdminPermission.VIEW_APPLICATIONS,
                    AdminPermission.VIEW_PARTICIPANTS,
                    AdminPermission.VIEW_FEEDBACK
            )),

    APPLICATION_REVIEWER(4, "Application Reviewer",
            "Can review and process applications",
            Set.of(
                    AdminPermission.REVIEW_APPLICATIONS,
                    AdminPermission.VIEW_EVENTS,
                    AdminPermission.VIEW_PARTICIPANTS
            )),

    CONTENT_MODERATOR(5, "Content Moderator",
            "Can manage feedback and testimonials",
            Set.of(
                    AdminPermission.MANAGE_FEEDBACK,
                    AdminPermission.VIEW_EVENTS,
                    AdminPermission.VIEW_PARTICIPANTS
            )),

    VIEWER(6, "Viewer",
            "Read-only access to system data",
            Set.of(
                    AdminPermission.VIEW_EVENTS,
                    AdminPermission.VIEW_APPLICATIONS,
                    AdminPermission.VIEW_PARTICIPANTS,
                    AdminPermission.VIEW_FEEDBACK,
                    AdminPermission.VIEW_REPORTS
            ));

    @Getter
    private final int level;
    @Getter
    private final String displayName;
    @Getter
    private final String description;
    private final Set<AdminPermission> defaultPermissions;

    AdminRole(int level, String displayName, String description, Set<AdminPermission> defaultPermissions) {
        this.level = level;
        this.displayName = displayName;
        this.description = description;
        this.defaultPermissions = new HashSet<>(defaultPermissions);
    }

    public Set<AdminPermission> getDefaultPermissions() {
        return new HashSet<>(defaultPermissions);
    }
}