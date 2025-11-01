package com.araw.araw.application.service;

import com.araw.araw.application.dto.application.ApplicationReviewNoteResponse;
import com.araw.araw.application.dto.application.CreateApplicationReviewNoteRequest;
import com.araw.araw.domain.admin.entity.Admin;
import com.araw.araw.domain.admin.repository.AdminRepository;
import com.araw.araw.domain.application.entity.Application;
import com.araw.araw.domain.application.entity.ApplicationReviewNote;
import com.araw.araw.domain.application.repository.ApplicationRepository;
import com.araw.araw.domain.application.repository.ApplicationReviewNoteRepository;
import com.araw.shared.exception.DomainNotFoundException;
import com.araw.shared.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationReviewNoteService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationReviewNoteRepository reviewNoteRepository;
    private final AdminRepository adminRepository;

    public ApplicationReviewNoteResponse addReviewNote(UUID applicationId,
                                                       CreateApplicationReviewNoteRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new DomainNotFoundException("Application not found: " + applicationId));

        Admin admin = adminRepository.findById(request.getAdminId())
                .orElseThrow(() -> new DomainNotFoundException("Admin not found: " + request.getAdminId()));

        if (request.getMaxScore() != null && request.getScore() != null
                && request.getScore() > request.getMaxScore()) {
            throw new DomainValidationException("Score cannot exceed max score");
        }

        String reviewerName = resolveReviewerName(request, admin);

        ApplicationReviewNote note = ApplicationReviewNote.builder()
                .application(application)
                .reviewer(admin)
                .reviewerName(reviewerName)
                .category(request.getCategory())
                .score(request.getScore())
                .maxScore(request.getMaxScore())
                .notes(request.getNotes().trim())
                .build();

        ApplicationReviewNote saved = reviewNoteRepository.save(note);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationReviewNoteResponse> getReviewNotes(UUID applicationId) {
        if (!applicationRepository.existsById(applicationId)) {
            throw new DomainNotFoundException("Application not found: " + applicationId);
        }
        return reviewNoteRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String resolveReviewerName(CreateApplicationReviewNoteRequest request, Admin admin) {
        if (request.getReviewerName() != null && !request.getReviewerName().isBlank()) {
            return request.getReviewerName().trim();
        }
        StringBuilder sb = new StringBuilder();
        if (admin.getFirstName() != null && !admin.getFirstName().isBlank()) {
            sb.append(admin.getFirstName());
        }
        if (admin.getLastName() != null) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(admin.getLastName());
        }
        if (sb.length() == 0) {
            return admin.getUsername() != null ? admin.getUsername() : admin.getEmail();
        }
        return sb.toString();
    }

    private ApplicationReviewNoteResponse toResponse(ApplicationReviewNote note) {
        return ApplicationReviewNoteResponse.builder()
                .id(note.getId())
                .category(note.getCategory())
                .score(note.getScore())
                .maxScore(note.getMaxScore())
                .notes(note.getNotes())
                .adminId(note.getReviewer() != null ? note.getReviewer().getId() : null)
                .reviewerName(note.getReviewerName())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
