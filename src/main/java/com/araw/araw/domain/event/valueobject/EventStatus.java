package com.araw.araw.domain.event.valueobject;

import lombok.Getter;

@Getter
public enum EventStatus {
    DRAFT("Draft"),
    UPCOMING("Upcoming"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    POSTPONED("Postponed");

    private final String displayName;

    EventStatus(String displayName) {
        this.displayName = displayName;
    }

}
