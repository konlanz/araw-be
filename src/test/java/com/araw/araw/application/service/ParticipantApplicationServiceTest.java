package com.araw.araw.application.service;

import com.araw.araw.application.dto.participant.ContactInfoDto;
import com.araw.araw.application.dto.participant.CreateParticipantRequest;
import com.araw.araw.application.dto.participant.ParticipantResponse;
import com.araw.araw.domain.application.valueobject.EducationLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ParticipantApplicationServiceTest {

    @Autowired
    private ParticipantApplicationService participantService;

    @Test
    void createAndFetchParticipant() {
        CreateParticipantRequest request = CreateParticipantRequest.builder()
                .firstName("Ama")
                .lastName("Mensah")
                .dateOfBirth(LocalDate.now().minusYears(16))
                .educationLevel(EducationLevel.HIGH_SCHOOL)
                .contactInfo(ContactInfoDto.builder()
                        .email("ama.mensah@example.com")
                        .phoneNumber("+233201234567")
                        .addressLine1("12 STEM Street")
                        .city("Accra")
                        .stateProvince("Greater Accra")
                        .country("Ghana")
                        .build())
                .interests(Set.of("robotics", "ai"))
                .skills(Set.of("python"))
                .consentForCommunication(true)
                .consentForPhotos(true)
                .consentForTestimonials(true)
                .build();

        ParticipantResponse created = participantService.createParticipant(request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getParticipantCode()).isNotBlank();
        assertThat(created.getInterests()).contains("robotics");

        ParticipantResponse fetched = participantService.getParticipant(created.getId());
        assertThat(fetched.getFirstName()).isEqualTo("Ama");
        assertThat(fetched.getMilestones()).isNotNull();
        assertThat(fetched.getEngagementScore()).isNotNull();
    }
}
