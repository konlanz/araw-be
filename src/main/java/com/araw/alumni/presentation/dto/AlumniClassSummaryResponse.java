package com.araw.alumni.presentation.dto;

import java.util.List;
import java.util.UUID;

public record AlumniClassSummaryResponse(
        UUID id,
        String name,
        Integer graduationYear,
        UUID coverMediaId,
        String description,
        int studentCount,
        List<AlumniClassStudentSummaryResponse> students
) {
}
