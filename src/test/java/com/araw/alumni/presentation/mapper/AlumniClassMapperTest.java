package com.araw.alumni.presentation.mapper;

import com.araw.alumni.domain.model.AlumniClass;
import com.araw.alumni.domain.model.AlumniClassStudent;
import com.araw.alumni.presentation.dto.AlumniClassSummaryResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlumniClassMapperTest {

    private final AlumniClassMapper mapper = new AlumniClassMapper();

    @Test
    void buildsSummaryWithOrderedStudentsAndProfileSnippet() {
        AlumniClass alumniClass = AlumniClass.create("Class of 2026", 2026, "Impact makers", null);

        AlumniClassStudent later = AlumniClassStudent.create(
                "Later Student",
                "Member",
                null,
                "Secondary profile",
                null,
                null,
                null,
                null,
                null,
                5
        );

        String longProfile = "Impact-driven leader mentoring across the continent. ".repeat(10);

        AlumniClassStudent first = AlumniClassStudent.create(
                "First Student",
                "Lead",
                null,
                longProfile,
                null,
                null,
                null,
                null,
                null,
                0
        );

        alumniClass.addStudent(later);
        alumniClass.addStudent(first);

        AlumniClassSummaryResponse response = mapper.toSummaryResponse(alumniClass);

        assertThat(response.studentCount()).isEqualTo(2);
        assertThat(response.students())
                .extracting(summary -> summary.fullName())
                .containsExactly("First Student", "Later Student");
        assertThat(response.students().get(0).profileSummary())
                .hasSizeLessThanOrEqualTo(200)
                .endsWith("…");
    }
}
