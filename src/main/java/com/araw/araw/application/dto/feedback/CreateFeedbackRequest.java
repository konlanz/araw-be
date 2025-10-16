package com.araw.araw.application.dto.feedback;

import com.araw.araw.domain.feedback.valueobject.FeedbackType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeedbackRequest {

    @NotNull(message = "Event ID is required")
    private UUID eventId;

    private UUID participantId;

    @NotBlank(message = "Name is required")
    private String submittedByName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String submittedByEmail;

    @NotNull(message = "Feedback type is required")
    private FeedbackType feedbackType;

    @NotNull(message = "Rating is required")
    private RatingDto rating;

    @Size(max = 2000, message = "Overall experience must not exceed 2000 characters")
    private String overallExperience;

    @Size(max = 2000, message = "What learned must not exceed 2000 characters")
    private String whatLearned;

    @Size(max = 1000, message = "Most valuable aspect must not exceed 1000 characters")
    private String mostValuableAspect;

    @Size(max = 2000, message = "Improvement suggestions must not exceed 2000 characters")
    private String improvementSuggestions;

    @NotNull(message = "Please indicate if you would recommend this event")
    private Boolean wouldRecommend;

    private String recommendationReason;

    private Set<String> skillsGained;

    private Map<String, Integer> aspectRatings;

    private Boolean isAnonymous = false;

    private Boolean consentToPublish = false;
}

