package com.araw.araw.application.dto.application;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewApplicationRequest {

    @NotNull(message = "Review score is required")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 10, message = "Score must not exceed 10")
    private Integer reviewScore;

    @Size(max = 2000, message = "Review notes must not exceed 2000 characters")
    private String reviewNotes;

    @NotBlank(message = "Reviewer name is required")
    private String reviewerName;
}
