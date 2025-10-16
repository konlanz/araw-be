package com.araw.araw.application.service;

import com.araw.araw.application.dto.application.ApplicantInfoDto;
import com.araw.araw.application.dto.application.ApplicationDecisionRequest;
import com.araw.araw.application.dto.application.CreateApplicationRequest;
import com.araw.araw.application.dto.application.ReviewApplicationRequest;
import com.araw.araw.application.dto.event.CreateEventRequest;
import com.araw.araw.application.dto.event.EventDateDto;
import com.araw.araw.application.dto.event.LocationDto;
import com.araw.araw.application.dto.participant.ContactInfoDto;
import com.araw.araw.application.dto.participant.CreateParticipantRequest;
import com.araw.araw.application.dto.application.ApplicationResponse;
import com.araw.araw.domain.application.valueobject.ApplicationStatus;
import com.araw.araw.domain.application.valueobject.EducationLevel;
import com.araw.araw.domain.event.valueobject.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ApplicationApplicationServiceTest {

    @Autowired
    private ApplicationApplicationService applicationService;

    @Autowired
    private EventApplicationService eventService;

    @Autowired
    private ParticipantApplicationService participantService;

    private UUID eventId;
    private UUID participantId;

    @BeforeEach
    void setUp() {
        CreateEventRequest eventRequest = CreateEventRequest.builder()
                .title("Design Challenge")
                .description("Design thinking challenge")
                .shortDescription("Design challenge")
                .eventType(EventType.WORKSHOP)
                .location(LocationDto.builder()
                        .venueName("Innovation Lab")
                        .addressLine1("100 Impact Ave")
                        .city("Accra")
                        .stateProvince("Greater Accra")
                        .country("Ghana")
                        .isVirtual(false)
                        .isHybrid(false)
                        .build())
                .isFree(true)
                .maxParticipants(20)
                .eventDates(List.of(EventDateDto.builder()
                        .sessionDate(LocalDateTime.now().plusDays(5))
                        .sessionEndDate(LocalDateTime.now().plusDays(5).plusHours(2))
                        .sessionName("Kick-off")
                        .build()))
                .build();

        eventId = eventService.createEvent(eventRequest).getId();

        CreateParticipantRequest participantRequest = CreateParticipantRequest.builder()
                .firstName("Yaw")
                .lastName("Boateng")
                .dateOfBirth(LocalDate.now().minusYears(17))
                .educationLevel(EducationLevel.HIGH_SCHOOL)
                .contactInfo(ContactInfoDto.builder()
                        .email("yaw.boateng@example.com")
                        .phoneNumber("+233209876543")
                        .addressLine1("45 Tech Road")
                        .city("Kumasi")
                        .stateProvince("Ashanti")
                        .country("Ghana")
                        .build())
                .interests(Set.of("design"))
                .skills(Set.of("cad"))
                .build();

        participantId = participantService.createParticipant(participantRequest).getId();
    }

    @Test
    void fullApplicationWorkflow() {
        CreateApplicationRequest request = CreateApplicationRequest.builder()
                .eventId(eventId)
                .participantId(participantId)
                .applicantInfo(ApplicantInfoDto.builder()
                        .firstName("Yaw")
                        .lastName("Boateng")
                        .dateOfBirth(LocalDate.now().minusYears(17))
                        .addressLine1("45 Tech Road")
                        .city("Kumasi")
                        .stateProvince("Ashanti")
                        .country("Ghana")
                        .schoolName("KNUST SHS")
                        .gradeLevel("11")
                        .phoneNumber("+233209876543")
                        .preferredLanguage("English")
                        .build())
                .email("yaw.boateng@example.com")
                .guardianConsent(true)
                .guardianName("Ama Boateng")
                .guardianEmail("ama.boateng@example.com")
                .guardianPhone("+233200000000")
                .emergencyContactName("Kojo Boateng")
                .emergencyContactPhone("+233201111111")
                .emergencyContactRelation("Father")
                .build();

        ApplicationResponse created = applicationService.createApplication(request);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(ApplicationStatus.DRAFT);

        ApplicationResponse submitted = applicationService.submitApplication(created.getId());
        assertThat(submitted.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);

        ReviewApplicationRequest reviewRequest = ReviewApplicationRequest.builder()
                .reviewScore(8)
                .reviewNotes("Strong motivation")
                .reviewerName("Admin Reviewer")
                .build();

        ApplicationResponse reviewed = applicationService.reviewApplication(created.getId(), reviewRequest);
        assertThat(reviewed.getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
        assertThat(reviewed.getReviewScore()).isEqualTo(8);

        ApplicationResponse accepted = applicationService.acceptApplication(created.getId());
        assertThat(accepted.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);

        ApplicationDecisionRequest cancelRequest = ApplicationDecisionRequest.builder()
                .reason("Candidate withdrew")
                .build();

        ApplicationResponse cancelled = applicationService.cancelApplication(created.getId(), cancelRequest.getReason());
        assertThat(cancelled.getStatus()).isEqualTo(ApplicationStatus.CANCELLED);
        assertThat(cancelled.getCancellationReason()).isEqualTo("Candidate withdrew");
    }
}
