package com.araw.alumni.application.command;

import java.util.List;
import java.util.UUID;

public record UpdateAlumniClassCommand(
        UUID id,
        String name,
        Integer graduationYear,
        String description,
        UUID coverMediaId,
        List<CreateAlumniClassCommand.StudentPayload> students,
        List<CreateAlumniClassCommand.GalleryItemPayload> galleryItems
) {
}
