package com.araw.araw.domain.event.valueobject;

import lombok.Getter;

@Getter
public enum EventType {
    WORKSHOP("Workshop"),
    BOOTCAMP("Boot Camp"),
    SUMMER_CAMP("Summer Camp"),
    AFTER_SCHOOL_PROGRAM("After School Program"),
    COMPETITION("Competition"),
    HACKATHON("Hackathon"),
    SEMINAR("Seminar"),
    WEBINAR("Webinar"),
    FIELD_TRIP("Field Trip"),
    MENTORSHIP_PROGRAM("Mentorship Program"),
    CONFERENCE("Conference");

    private final String displayName;

    EventType(String displayName) {
        this.displayName = displayName;
    }

}