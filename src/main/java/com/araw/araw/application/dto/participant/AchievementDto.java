package com.araw.araw.application.dto.participant;

import com.araw.araw.domain.participant.valueobject.AchievementLevel;
import com.araw.araw.domain.participant.valueobject.AchievementType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementDto {
    private UUID id;
    private String achievementCode;
    private String title;
    private String description;
    private AchievementType achievementType;
    private AchievementLevel achievementLevel;
    private String category;
    private LocalDateTime earnedDate;
    private LocalDateTime expiryDate;
    private String badgeImageUrl;
    private String certificateUrl;
    private String certificateNumber;
    private Set<String> skillsDemonstrated;
    private Integer score;
    private Integer maxScore;
    private Double percentage;
    private Integer percentile;
    private Boolean isVerified;
    private Boolean isValid;
    private Boolean isFeatured;
}

