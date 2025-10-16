package com.araw.araw.application.service;

import com.araw.araw.application.dto.participant.CreateParticipantRequest;
import com.araw.araw.application.dto.participant.ParticipantResponse;
import com.araw.araw.application.dto.participant.ParticipantSummaryResponse;
import com.araw.araw.application.dto.participant.UpdateParticipantRequest;
import com.araw.araw.application.mapper.ParticipantMapper;
import com.araw.araw.domain.participant.enitity.Participant;
import com.araw.araw.domain.participant.repository.ParticipantRepository;
import com.araw.araw.domain.participant.service.ParticipantDomainService;
import com.araw.shared.exception.DomainNotFoundException;
import com.araw.shared.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.ArrayList;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantApplicationService {

    private final ParticipantRepository participantRepository;
    private final ParticipantMapper participantMapper;
    private final ParticipantDomainService participantDomainService;

    public ParticipantResponse createParticipant(CreateParticipantRequest request) {
        validateEmailUniqueness(null, request.getContactInfo() != null ? request.getContactInfo().getEmail() : null);
        Participant participant = participantMapper.toEntity(request);
        initializeCollections(participant);
        Participant saved = participantRepository.save(participant);

        participantDomainService.evaluateForAlumniStatus(saved);
        participantRepository.flush();

        return enhanceParticipantResponse(saved);
    }

    public ParticipantResponse updateParticipant(UUID participantId, UpdateParticipantRequest request) {
        Participant participant = getParticipantEntity(participantId);
        if (request.getContactInfo() != null) {
            validateEmailUniqueness(participantId, request.getContactInfo().getEmail());
        }

        participantMapper.updateEntity(participant, request);
        initializeCollections(participant);
        Participant saved = participantRepository.save(participant);
        participantDomainService.evaluateForAlumniStatus(saved);

        return enhanceParticipantResponse(saved);
    }

    @Transactional(readOnly = true)
    public ParticipantResponse getParticipant(UUID participantId) {
        Participant participant = getParticipantEntity(participantId);
        return enhanceParticipantResponse(participant);
    }

    @Transactional(readOnly = true)
    public ParticipantResponse getParticipantByCode(String code) {
        Participant participant = participantRepository.findByParticipantCode(code)
                .orElseThrow(() -> new DomainNotFoundException("Participant not found for code: " + code));
        return enhanceParticipantResponse(participant);
    }

    @Transactional(readOnly = true)
    public Page<ParticipantSummaryResponse> listParticipants(String searchTerm, Pageable pageable) {
        Page<Participant> page;
        if (searchTerm != null && !searchTerm.isBlank()) {
            page = participantRepository.searchParticipants(searchTerm.trim(), pageable);
        } else {
            page = participantRepository.findAll(pageable);
        }
        return page.map(participantMapper::toSummaryResponse);
    }

    public void deleteParticipant(UUID participantId) {
        Participant participant = getParticipantEntity(participantId);
        participantRepository.delete(participant);
    }

    private ParticipantResponse enhanceParticipantResponse(Participant participant) {
        ParticipantResponse response = participantMapper.toResponse(participant);
        response.setEngagementScore(participantDomainService.calculateEngagementScore(participant));
        response.setMilestones(participantDomainService.getParticipantMilestones(participant));
        return response;
    }

    private Participant getParticipantEntity(UUID participantId) {
        return participantRepository.findById(participantId)
                .orElseThrow(() -> new DomainNotFoundException("Participant not found: " + participantId));
    }

    private void validateEmailUniqueness(UUID participantId, String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        boolean exists = participantId == null
                ? participantRepository.existsByContactInfoEmail(email)
                : participantRepository.existsByContactInfoEmailAndIdNot(email, participantId);
        if (exists) {
            throw new DomainValidationException("Participant with email already exists: " + email);
        }
    }

    private void initializeCollections(Participant participant) {
        if (participant.getInterests() == null) {
            participant.setInterests(new HashSet<>());
        }
        if (participant.getSkills() == null) {
            participant.setSkills(new HashSet<>());
        }
        if (participant.getAttendedEvents() == null) {
            participant.setAttendedEvents(new HashSet<>());
        }
        if (participant.getAchievements() == null) {
            participant.setAchievements(new ArrayList<>());
        }
        if (participant.getProgressRecords() == null) {
            participant.setProgressRecords(new ArrayList<>());
        }
        if (participant.getTotalEventsAttended() == null) {
            participant.setTotalEventsAttended(0);
        }
        if (participant.getTotalHoursParticipated() == null) {
            participant.setTotalHoursParticipated(0);
        }
        if (participant.getIsAlumni() == null) {
            participant.setIsAlumni(false);
        }
        if (participant.getConsentForCommunication() == null) {
            participant.setConsentForCommunication(true);
        }
        if (participant.getConsentForPhotos() == null) {
            participant.setConsentForPhotos(true);
        }
        if (participant.getConsentForTestimonials() == null) {
            participant.setConsentForTestimonials(true);
        }
    }
}
