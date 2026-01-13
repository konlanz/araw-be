package com.araw.alumni.presentation.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AlumniClassResponse(
        UUID id,
        String name,
        Integer graduationYear,
        String description,
        UUID coverMediaId,
        List<AlumniClassStudentResponse> students,
        List<AlumniClassGalleryItemResponse> galleryItems,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
