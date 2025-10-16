package com.araw.araw.application.dto.feedback;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeedbackRequest {

    @Valid
    private RatingDto rating;

    @Size(max = 2000)
    private String overallExperience;

    @Size(max = 2000)
    private String whatLearned;

    @Size(max = 1000)
    private String mostValuableAspect;

    @Size(max = 2000)
    private String improvementSuggestions;

    private Boolean wouldRecommend;

    private String recommendationReason;

    private Set<String> skillsGained;

    private Map<String, Integer> aspectRatings;

    private Boolean consentToPublish;

    private Boolean isFeatured;

    private Boolean followUpCompleted;

    private String followUpNotes;

    private Boolean publish;
}
