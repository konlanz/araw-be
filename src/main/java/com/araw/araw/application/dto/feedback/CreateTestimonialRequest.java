package com.araw.araw.application.dto.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestimonialRequest {

    @NotBlank(message = "Testimonial quote is required")
    @Size(max = 2000, message = "Quote must not exceed 2000 characters")
    private String quote;

    @Size(max = 500, message = "Context must not exceed 500 characters")
    private String context;

    @Size(max = 160, message = "Author name must not exceed 160 characters")
    private String authorName;

    @Size(max = 160, message = "Author title must not exceed 160 characters")
    private String authorTitle;

    @Size(max = 500, message = "Author photo URL must not exceed 500 characters")
    private String authorPhotoUrl;

    @Size(max = 500, message = "Highlight text must not exceed 500 characters")
    private String highlightText;

    @Size(max = 500, message = "Video URL must not exceed 500 characters")
    private String videoTestimonialUrl;

    private Boolean isFeatured;
    private Boolean publish;
    private Integer displayOrder;
}
