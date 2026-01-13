package com.araw.alumni.application.command;

import java.util.List;
import java.util.UUID;

public record CreateAlumniClassCommand(
        String name,
        Integer graduationYear,
        String description,
        UUID coverMediaId,
        List<StudentPayload> students,
        List<GalleryItemPayload> galleryItems
) {

    public record StudentPayload(
            UUID id,
            String fullName,
            String role,
            String quote,
            String profile,
            String capstoneProjectDescription,
            String schoolAttended,
            String currentLocation,
            String projectWorkedOn,
            UUID photoMediaId,
            Integer displayOrder
    ) {
    }

    public record GalleryItemPayload(
            UUID id,
            UUID mediaId,
            String caption,
            Integer displayOrder
    ) {
    }
}
