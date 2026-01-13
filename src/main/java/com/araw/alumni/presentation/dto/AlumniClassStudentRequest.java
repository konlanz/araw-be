package com.araw.alumni.presentation.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AlumniClassStudentRequest(
        UUID id,
        @NotBlank(message = "Full name is required")
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
