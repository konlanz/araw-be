package com.araw.araw.domain.participant.valueobject;

import lombok.Getter;

@Getter
public enum AchievementType {
    COMPLETION("Course/Event Completion"),
    CERTIFICATION("Certification"),
    AWARD("Award or Recognition"),
    MILESTONE("Learning Milestone"),
    PROJECT("Project Completion"),
    COMPETITION("Competition Achievement"),
    SKILL_BADGE("Skill Badge"),
    PARTICIPATION("Participation"),
    LEADERSHIP("Leadership Recognition"),
    INNOVATION("Innovation Award"),
    EXCELLENCE("Excellence Award"),
    IMPROVEMENT("Most Improved"),
    COLLABORATION("Team Collaboration"),
    MENTORSHIP("Mentorship Achievement"),
    SCHOLARSHIP("Scholarship Award");

    private final String displayName;

    AchievementType(String displayName) {
        this.displayName = displayName;
    }

    public boolean isCertifiable() {
        return this == CERTIFICATION ||
                this == COMPLETION ||
                this == AWARD ||
                this == SCHOLARSHIP;
    }
}

