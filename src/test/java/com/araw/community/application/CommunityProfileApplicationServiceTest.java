package com.araw.community.application;

import com.araw.community.application.command.CreateCommunityProfileCommand;
import com.araw.community.application.command.UpdateCommunityProfileCommand;
import com.araw.community.domain.model.CommunityProfile;
import com.araw.community.domain.model.ProfileType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CommunityProfileApplicationServiceTest {

    @Autowired
    private CommunityProfileApplicationService profileService;

    @Test
    void createAndSearchProfile() {
        CommunityProfile created = profileService.createProfile(new CreateCommunityProfileCommand(
                "Ama",
                "Mensah",
                ProfileType.VOLUNTEER,
                OffsetDateTime.now(),
                "ama.mensah@example.com",
                "+233201234567",
                "STEM coach",
                "Committed to inspiring young girls.",
                "ARAW",
                "Accra, Ghana",
                Set.of("mentoring", "robotics"),
                Set.of("education"),
                null,
                false,
                null
        ));

        assertThat(created.getSlug()).isEqualTo("ama-mensah");
        assertThat(created.isActive()).isTrue();

        CommunityProfile featured = profileService.updateProfile(new UpdateCommunityProfileCommand(
                created.getId(),
                created.getFirstName(),
                created.getLastName(),
                created.getProfileType(),
                created.getJoinedAt(),
                created.getEmail(),
                created.getPhone(),
                created.getHeadline(),
                created.getBiography(),
                created.getOrganization(),
                created.getLocation(),
                created.getSkills(),
                created.getInterests(),
                created.getProfileMediaId(),
                true,
                true,
                created.getSlug()
        ));

        assertThat(featured.isFeatured()).isTrue();

        Page<CommunityProfile> searchResult = profileService.search(
                ProfileType.VOLUNTEER,
                true,
                true,
                PageRequest.of(0, 5)
        );

        assertThat(searchResult.getContent())
                .extracting(CommunityProfile::getId)
                .contains(featured.getId());
    }
}
