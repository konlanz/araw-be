package com.araw.araw.domain.application.valueobject;

import lombok.Getter;

@Getter
public enum ApplicationStatus {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    WAITLISTED("Waitlisted"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    WITHDRAWN("Withdrawn");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean isTerminal() {
        return this == REJECTED || this == CANCELLED || this == WITHDRAWN;
    }

    public boolean canTransitionTo(ApplicationStatus newStatus) {
        if (this.isTerminal()) {
            return false;
        }

        return switch (this) {
            case DRAFT -> newStatus == SUBMITTED || newStatus == WITHDRAWN;
            case SUBMITTED -> newStatus == UNDER_REVIEW || newStatus == ACCEPTED ||
                    newStatus == REJECTED || newStatus == WAITLISTED || newStatus == WITHDRAWN;
            case UNDER_REVIEW -> newStatus == ACCEPTED || newStatus == REJECTED ||
                    newStatus == WAITLISTED || newStatus == WITHDRAWN;
            case ACCEPTED -> newStatus == CONFIRMED || newStatus == CANCELLED;
            case WAITLISTED -> newStatus == ACCEPTED || newStatus == REJECTED || newStatus == WITHDRAWN;
            case CONFIRMED -> newStatus == CANCELLED;
            default -> false;
        };
    }
}

