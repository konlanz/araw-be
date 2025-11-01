package com.araw.araw.application.dto.application;

import com.araw.araw.domain.application.valueobject.ApplicationReviewCategory;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApplicationReviewNoteRequest {

    @NotNull(message = "Category is required")
    private ApplicationReviewCategory category;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    @NotBlank(message = "Notes are required")
    private String notes;

    @Min(value = 0, message = "Score must be zero or positive")
    private Integer score;

    @Min(value = 1, message = "Max score must be at least 1")
    private Integer maxScore;

    @NotNull(message = "Admin ID is required")
    private UUID adminId;

    @Size(max = 160, message = "Reviewer name must not exceed 160 characters")
    private String reviewerName;
}
