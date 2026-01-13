package com.araw.alumni.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateAlumniClassRequest(
        @NotBlank(message = "Class name is required")
        String name,
        @NotNull(message = "Graduation year is required")
        Integer graduationYear,
        String description,
        UUID coverMediaId,
        @Valid
        List<AlumniClassStudentRequest> students,
        @Valid
        List<AlumniClassGalleryItemRequest> galleryItems
) {
}
