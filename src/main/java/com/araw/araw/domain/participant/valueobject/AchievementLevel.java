package com.araw.araw.domain.participant.valueobject;

import lombok.Getter;

@Getter
public enum AchievementLevel {
    BRONZE("Bronze", 1, "Basic achievement level"),
    SILVER("Silver", 2, "Intermediate achievement level"),
    GOLD("Gold", 3, "Advanced achievement level"),
    PLATINUM("Platinum", 4, "Expert achievement level"),
    DIAMOND("Diamond", 5, "Master achievement level");

    private final String displayName;
    private final int tier;
    private final String description;

    AchievementLevel(String displayName, int tier, String description) {
        this.displayName = displayName;
        this.tier = tier;
        this.description = description;
    }

    public boolean isHigherThan(AchievementLevel other) {
        return this.tier > other.tier;
    }

    public static AchievementLevel fromScore(double percentage) {
        if (percentage >= 95) return DIAMOND;
        if (percentage >= 85) return PLATINUM;
        if (percentage >= 75) return GOLD;
        if (percentage >= 65) return SILVER;
        return BRONZE;
    }
}
