package com.araw.araw.application.service;

import com.araw.araw.application.dto.application.ApplicationDocumentDto;
import com.araw.araw.application.dto.application.ApplicationResponse;
import com.araw.araw.domain.application.entity.Application;
import com.araw.araw.domain.application.entity.ApplicationDocument;
import com.araw.araw.domain.application.repository.ApplicationRepository;
import com.araw.media.application.MediaStorageService;
import com.araw.media.application.command.MediaUploadCommand;
import com.araw.media.domain.exception.MediaStorageException;
import com.araw.media.domain.model.MediaAsset;
import com.araw.media.domain.model.MediaCategory;
import com.araw.shared.exception.DomainNotFoundException;
import com.araw.shared.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ApplicationDocumentService {

    private static final long MAX_RESUME_SIZE_BYTES = 50L * 1024 * 1024; // 50 MB
    private static final Set<String> ALLOWED_RESUME_EXTENSIONS = Set.of("pdf", "doc", "docx");
    private static final String RESUME_DOCUMENT_TYPE = "RESUME";

    private final ApplicationRepository applicationRepository;
    private final MediaStorageService mediaStorageService;

    public ApplicationDocument attachResume(UUID applicationId, MultipartFile resumeFile) {
        if (resumeFile == null || resumeFile.isEmpty()) {
            return null;
        }

        validateResumeFile(resumeFile);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new DomainNotFoundException("Application not found: " + applicationId));

        removeExistingResume(application);

        MediaAsset asset = storeResumeInMediaStorage(application, resumeFile);

        ApplicationDocument document = ApplicationDocument.builder()
                .documentType(RESUME_DOCUMENT_TYPE)
                .fileName(asset.getFileName())
                .fileUrl(asset.getObjectKey())
                .fileSize(asset.getFileSize())
                .mimeType(asset.getContentType())
                .mediaAssetId(asset.getId())
                .build();

        application.addDocument(document);
        applicationRepository.save(application);
        return document;
    }

    @Transactional(readOnly = true)
    public String generateDownloadUrl(UUID applicationId, UUID documentId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new DomainNotFoundException("Application not found: " + applicationId));

        return application.getDocuments().stream()
                .filter(doc -> Objects.equals(doc.getId(), documentId))
                .findFirst()
                .map(this::toDownloadUrl)
                .orElseThrow(() -> new DomainNotFoundException(
                        "Document " + documentId + " not found for application " + applicationId));
    }

    @Transactional(readOnly = true)
    public void populateDownloadUrls(ApplicationResponse response) {
        if (response == null) {
            return;
        }
        populateDownloadUrls(response.getDocuments());
    }

    @Transactional(readOnly = true)
    public void populateDownloadUrls(Collection<ApplicationResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return;
        }
        responses.forEach(this::populateDownloadUrls);
    }

    @Transactional(readOnly = true)
    public void populateDownloadUrls(List<ApplicationDocumentDto> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        documents.forEach(doc -> {
            if (doc.getMediaAssetId() != null) {
                try {
                    doc.setDownloadUrl(mediaStorageService.generatePresignedUrl(doc.getMediaAssetId()));
                } catch (MediaStorageException ex) {
                    log.warn("Failed to generate download URL for application document {}", doc.getId(), ex);
                }
            }
        });
    }

    private void validateResumeFile(MultipartFile resumeFile) {
        if (resumeFile.getSize() > MAX_RESUME_SIZE_BYTES) {
            throw new DomainValidationException("Resume file exceeds the maximum size of 50 MB");
        }

        String filename = resumeFile.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new DomainValidationException("Resume file name cannot be empty");
        }

        String extension = extractExtension(filename);
        if (!ALLOWED_RESUME_EXTENSIONS.contains(extension)) {
            throw new DomainValidationException("Unsupported resume file type: ." + extension +
                    ". Allowed types are pdf, doc, docx");
        }
    }

    private void removeExistingResume(Application application) {
        List<ApplicationDocument> toRemove = new ArrayList<>();
        for (ApplicationDocument document : application.getDocuments()) {
            if (RESUME_DOCUMENT_TYPE.equalsIgnoreCase(document.getDocumentType())) {
                if (document.getMediaAssetId() != null) {
                    try {
                        mediaStorageService.deleteAsset(document.getMediaAssetId());
                    } catch (MediaStorageException ex) {
                        log.warn("Failed to delete existing resume asset {} for application {}",
                                document.getMediaAssetId(), application.getId(), ex);
                    }
                }
                toRemove.add(document);
            }
        }
        toRemove.forEach(application::removeDocument);
    }

    private MediaAsset storeResumeInMediaStorage(Application application, MultipartFile resumeFile) {
        try (MediaUploadCommand command = new MediaUploadCommand(
                resumeFile.getOriginalFilename(),
                resumeFile.getContentType(),
                resumeFile.getSize(),
                resumeFile.getInputStream(),
                MediaCategory.APPLICATION_DOCUMENT,
                "Resume for application " + application.getApplicationNumber()
        )) {
            return mediaStorageService.storeMedia(command);
        } catch (IOException ex) {
            throw new DomainValidationException("Failed to read resume file for upload", ex);
        }
    }

    private String toDownloadUrl(ApplicationDocument document) {
        if (document.getMediaAssetId() == null) {
            throw new DomainValidationException("Document is missing media storage reference");
        }
        return mediaStorageService.generatePresignedUrl(document.getMediaAssetId());
    }

    private String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }
}
