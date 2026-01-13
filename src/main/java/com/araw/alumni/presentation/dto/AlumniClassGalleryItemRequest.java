package com.araw.alumni.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AlumniClassGalleryItemRequest(
        UUID id,
        @NotNull(message = "Gallery media id is required")
        UUID mediaId,
        String caption,
        Integer displayOrder
) {
}
