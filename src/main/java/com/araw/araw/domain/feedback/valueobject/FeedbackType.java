package com.araw.araw.domain.feedback.valueobject;

import lombok.Getter;

@Getter
public enum FeedbackType {
    IMMEDIATE("Immediate Feedback"),
    POST_EVENT("Post-Event Feedback"),
    FOLLOW_UP("Follow-up Feedback"),
    LONG_TERM("Long-term Impact Feedback"),
    PARENT_GUARDIAN("Parent/Guardian Feedback"),
    INSTRUCTOR("Instructor Feedback"),
    MENTOR("Mentor Feedback");

    private final String displayName;

    FeedbackType(String displayName) {
        this.displayName = displayName;
    }

}
