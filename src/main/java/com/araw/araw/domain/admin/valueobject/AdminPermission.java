package com.araw.araw.domain.admin.valueobject;

import lombok.Getter;

@Getter
public enum AdminPermission {
    MANAGE_ADMINS("Manage admin accounts"),
    VIEW_ADMINS("View admin accounts"),

    MANAGE_EVENTS("Create, update, and delete events"),
    VIEW_EVENTS("View events"),
    PUBLISH_EVENTS("Publish and unpublish events"),

    REVIEW_APPLICATIONS("Review and process applications"),
    VIEW_APPLICATIONS("View applications"),
    EXPORT_APPLICATIONS("Export application data"),

    MANAGE_PARTICIPANTS("Manage participant records"),
    VIEW_PARTICIPANTS("View participant records"),
    EXPORT_PARTICIPANTS("Export participant data"),

    MANAGE_FEEDBACK("Manage feedback and testimonials"),
    VIEW_FEEDBACK("View feedback"),
    PUBLISH_TESTIMONIALS("Publish testimonials"),

    VIEW_REPORTS("View system reports"),
    EXPORT_REPORTS("Export reports"),

    VIEW_ANALYTICS("View analytics dashboard"),
    MANAGE_SETTINGS("Manage system settings"),
    EXPORT_DATA("Export system data"),
    VIEW_AUDIT_LOGS("View audit logs");

    private final String description;

    AdminPermission(String description) {
        this.description = description;
    }

}