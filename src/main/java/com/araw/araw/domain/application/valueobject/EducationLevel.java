package com.araw.araw.domain.application.valueobject;

import lombok.Getter;

@Getter
public enum EducationLevel {
    ELEMENTARY_SCHOOL("Elementary School"),
    MIDDLE_SCHOOL("Middle School"),
    HIGH_SCHOOL("High School"),
    UNDERGRADUATE("Undergraduate"),
    GRADUATE("Graduate"),
    POSTGRADUATE("Postgraduate"),
    OTHER("Other");

    private final String displayName;

    EducationLevel(String displayName) {
        this.displayName = displayName;
    }

}
