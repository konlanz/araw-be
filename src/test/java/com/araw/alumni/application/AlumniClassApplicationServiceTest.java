package com.araw.alumni.application;

import com.araw.alumni.application.command.CreateAlumniClassCommand;
import com.araw.alumni.application.command.UpdateAlumniClassCommand;
import com.araw.alumni.domain.model.AlumniClass;
import com.araw.alumni.domain.model.AlumniClassStudent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AlumniClassApplicationServiceTest {

    @Autowired
    private AlumniClassApplicationService alumniClassService;

    @Test
    void createUpdateAndListAlumniClasses() {
        CreateAlumniClassCommand createCommand = new CreateAlumniClassCommand(
                "Class of 2020",
                2020,
                "Trailblazing alumni cohort",
                UUID.randomUUID(),
                List.of(
                        new CreateAlumniClassCommand.StudentPayload(
                                null,
                                "Ama Mensah",
                                "President",
                                "Leading the path for others.",
                                "Mentor inspiring future scientists.",
                                "Solar irrigation capstone",
                                "University of Ghana",
                                "ARAW Innovation Hub, Accra",
                                "Solar irrigation prototype",
                                UUID.randomUUID(),
                                0
                        ),
                        new CreateAlumniClassCommand.StudentPayload(
                                null,
                                "Yaw Owusu",
                                "Secretary",
                                "Documenting every milestone.",
                                "STEM advocate documenting team achievements.",
                                "Robotics documentation capstone",
                                "Kwame Nkrumah University of Science and Technology",
                                "Ashesi Entrepreneurship Lab",
                                "Community robotics challenge",
                                UUID.randomUUID(),
                                1
                        )
                ),
                List.of(
                        new CreateAlumniClassCommand.GalleryItemPayload(
                                null,
                                UUID.randomUUID(),
                                "Orientation day highlights",
                                0
                        )
                )
        );

        AlumniClass created = alumniClassService.createClass(createCommand);

        assertThat(created.getGraduationYear()).isEqualTo(2020);
        assertThat(created.getStudents()).hasSize(2);
        assertThat(created.getGalleryItems()).hasSize(1);
        assertThat(created.getStudents().get(0).getProfile()).isEqualTo("Mentor inspiring future scientists.");
        assertThat(created.getStudents().get(0).getSchoolAttended()).isEqualTo("University of Ghana");
        assertThat(created.getStudents().get(0).getCurrentLocation()).isEqualTo("ARAW Innovation Hub, Accra");
        assertThat(created.getStudents().get(0).getProjectWorkedOn()).isEqualTo("Solar irrigation prototype");

        AlumniClassStudent firstStudent = created.getStudents().get(0);

        UpdateAlumniClassCommand updateCommand = new UpdateAlumniClassCommand(
                created.getId(),
                "Class of 2020",
                2020,
                "Updated journey of the class.",
                created.getCoverMediaId(),
                List.of(
                        new CreateAlumniClassCommand.StudentPayload(
                                firstStudent.getId(),
                                "Ama Mensah",
                                "Alumni Lead",
                                "Still leading the path.",
                                "Mentor inspiring future scientists.",
                                firstStudent.getCapstoneProjectDescription(),
                                "University of Ghana",
                                "ARAW Innovation Hub, Accra",
                                "Solar irrigation prototype",
                                firstStudent.getPhotoMediaId(),
                                1
                        ),
                        new CreateAlumniClassCommand.StudentPayload(
                                null,
                                "Kojo Addo",
                                "Mentor",
                                "Supporting the next leaders.",
                                "Software engineer volunteering evenings.",
                                "Mentorship capstone",
                                "Ashesi University",
                                "Berlin Startup Studio",
                                "STEM career mentorship series",
                                UUID.randomUUID(),
                                0
                        )
                ),
                List.of()
        );

        AlumniClass updated = alumniClassService.updateClass(updateCommand);

        assertThat(updated.getDescription()).contains("Updated journey");
        assertThat(updated.getStudents()).hasSize(2);
        assertThat(updated.getStudents().get(0).getFullName()).isEqualTo("Kojo Addo");
        assertThat(updated.getStudents().get(0).getDisplayOrder()).isZero();
        assertThat(updated.getStudents().get(0).getProjectWorkedOn()).isEqualTo("STEM career mentorship series");

        alumniClassService.createClass(new CreateAlumniClassCommand(
                "Class of 2022",
                2022,
                "Newest alumni cohort",
                null,
                List.of(),
                List.of()
        ));

        List<AlumniClass> ordered = alumniClassService.findAllOrderedByGraduationYear();
        assertThat(ordered)
                .extracting(AlumniClass::getGraduationYear)
                .containsExactly(2022, 2020);
    }

    @Test
    void addStudentAppendsWithDefaultOrderAndListsStudents() {
        AlumniClass created = alumniClassService.createClass(new CreateAlumniClassCommand(
                "Class of 2024",
                2024,
                "Recent cohort",
                null,
                List.of(),
                List.of()
        ));

        var firstStudentPayload = new CreateAlumniClassCommand.StudentPayload(
                null,
                "First Student",
                "Lead",
                "Quote",
                "Profile",
                "Capstone",
                "School",
                "Location",
                "Project",
                null,
                null
        );

        var addedFirst = alumniClassService.addStudent(created.getId(), firstStudentPayload);
        assertThat(addedFirst.getDisplayOrder()).isZero();

        var secondStudentPayload = new CreateAlumniClassCommand.StudentPayload(
                null,
                "Second Student",
                "Member",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        var addedSecond = alumniClassService.addStudent(created.getId(), secondStudentPayload);
        assertThat(addedSecond.getDisplayOrder()).isEqualTo(1);

        List<AlumniClassStudent> students = alumniClassService.listStudents(created.getId());
        assertThat(students)
                .extracting(AlumniClassStudent::getFullName)
                .containsExactly("First Student", "Second Student");
    }

    @Test
    void updateStudentAllowsEditingAndReordering() {
        AlumniClass created = alumniClassService.createClass(new CreateAlumniClassCommand(
                "Class of 2025",
                2025,
                "Cohort focused on product launches",
                null,
                List.of(
                        new CreateAlumniClassCommand.StudentPayload(
                                null,
                                "Abena Boateng",
                                "Coordinator",
                                "Charting new paths.",
                                "Hands-on builder leading community initiatives.",
                                "Prototype build capstone",
                                "University of Ghana",
                                "Remote - Accra",
                                "Prototype bootcamp",
                                null,
                                0
                        ),
                        new CreateAlumniClassCommand.StudentPayload(
                                null,
                                "Dela Torgbor",
                                "Mentor",
                                "Guiding the team.",
                                "Community organizer with flair for detail.",
                                "Design strategy capstone",
                                "KNUST",
                                "Remote - Kumasi",
                                "Design sprints",
                                null,
                                1
                        )
                ),
                List.of()
        ));

        AlumniClassStudent studentToUpdate = created.getStudents().get(0);
        AlumniClassStudent peer = created.getStudents().get(1);

        String updatedProfile = "Expanded profile describing leadership and alumni impact across regions.";

        var payload = new CreateAlumniClassCommand.StudentPayload(
                studentToUpdate.getId(),
                "Abena Boateng (Updated)",
                "Lead Coordinator",
                studentToUpdate.getQuote(),
                updatedProfile,
                studentToUpdate.getCapstoneProjectDescription(),
                studentToUpdate.getSchoolAttended(),
                "Remote - Dakar",
                "Regional mentorship program",
                studentToUpdate.getPhotoMediaId(),
                2
        );

        AlumniClassStudent updated = alumniClassService.updateStudent(created.getId(), studentToUpdate.getId(), payload);

        assertThat(updated.getFullName()).contains("Updated");
        assertThat(updated.getCurrentLocation()).isEqualTo("Remote - Dakar");
        assertThat(updated.getDisplayOrder()).isEqualTo(2);

        List<AlumniClassStudent> ordered = alumniClassService.listStudents(created.getId());
        assertThat(ordered)
                .extracting(AlumniClassStudent::getFullName)
                .containsExactly(peer.getFullName(), updated.getFullName());
    }
}
