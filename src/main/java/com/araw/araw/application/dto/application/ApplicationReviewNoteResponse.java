package com.araw.araw.application.dto.application;

import com.araw.araw.domain.application.valueobject.ApplicationReviewCategory;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class ApplicationReviewNoteResponse {
    UUID id;
    ApplicationReviewCategory category;
    Integer score;
    Integer maxScore;
    String notes;
    UUID adminId;
    String reviewerName;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
