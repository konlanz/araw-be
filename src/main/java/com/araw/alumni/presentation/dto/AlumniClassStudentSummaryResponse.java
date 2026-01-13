package com.araw.alumni.presentation.dto;

import java.util.UUID;

public record AlumniClassStudentSummaryResponse(
        UUID id,
        String fullName,
        String role,
        String profileSummary,
        UUID photoMediaId,
        Integer displayOrder
) {
}
