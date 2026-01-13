package com.araw.alumni.presentation.dto;

import java.util.UUID;

public record AlumniClassGalleryItemResponse(
        UUID id,
        UUID mediaId,
        String caption,
        Integer displayOrder
) {
}
