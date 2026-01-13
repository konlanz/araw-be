package com.araw.alumni.presentation.dto;

import java.util.UUID;

public record AlumniClassStudentResponse(
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
