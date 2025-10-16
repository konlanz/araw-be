package com.araw.araw.application.dto.feedback;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialDto {
    private UUID id;
    private String quote;
    private String context;
    private String authorName;
    private String authorTitle;
    private String authorPhotoUrl;
    private String highlightText;
    private String videoTestimonialUrl;
    private Boolean isFeatured;
    private Integer displayOrder;
    private Boolean isPublished;
    private LocalDateTime createdAt;
}
