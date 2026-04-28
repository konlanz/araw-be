package com.araw.alumni.presentation.mapper;

import com.araw.alumni.application.command.CreateAlumniClassCommand;
import com.araw.alumni.application.command.UpdateAlumniClassCommand;
import com.araw.alumni.domain.model.AlumniClass;
import com.araw.alumni.domain.model.AlumniClassGalleryItem;
import com.araw.alumni.domain.model.AlumniClassStudent;
import com.araw.alumni.presentation.dto.AlumniClassGalleryItemRequest;
import com.araw.alumni.presentation.dto.AlumniClassGalleryItemResponse;
import com.araw.alumni.presentation.dto.AlumniClassResponse;
import com.araw.alumni.presentation.dto.AlumniClassStudentRequest;
import com.araw.alumni.presentation.dto.AlumniClassStudentResponse;
import com.araw.alumni.presentation.dto.AlumniClassStudentSummaryResponse;
import com.araw.alumni.presentation.dto.AlumniClassSummaryResponse;
import com.araw.alumni.presentation.dto.CreateAlumniClassRequest;
import com.araw.alumni.presentation.dto.UpdateAlumniClassRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AlumniClassMapper {

    private static final int PROFILE_SUMMARY_MAX_LENGTH = 200;

    public CreateAlumniClassCommand toCreateCommand(CreateAlumniClassRequest request) {
        return new CreateAlumniClassCommand(
                request.name(),
                request.graduationYear(),
                request.description(),
                request.coverMediaId(),
                request.students() != null ? toStudentPayloads(request.students()) : null,
                request.galleryItems() != null ? toGalleryPayloads(request.galleryItems()) : null
        );
    }

    public UpdateAlumniClassCommand toUpdateCommand(UUID id, UpdateAlumniClassRequest request) {
        return new UpdateAlumniClassCommand(
                id,
                request.name(),
                request.graduationYear(),
                request.description(),
                request.coverMediaId(),
                request.students() != null ? toStudentPayloads(request.students()) : null,
                request.galleryItems() != null ? toGalleryPayloads(request.galleryItems()) : null
        );
    }

    public AlumniClassResponse toResponse(AlumniClass alumniClass) {
        return new AlumniClassResponse(
                alumniClass.getId(),
                alumniClass.getName(),
                alumniClass.getGraduationYear(),
                alumniClass.getDescription(),
                alumniClass.getCoverMediaId(),
                toStudentResponses(alumniClass.getStudents()),
                toGalleryResponses(alumniClass.getGalleryItems()),
                alumniClass.getCreatedAt(),
                alumniClass.getUpdatedAt()
        );
    }

    public AlumniClassSummaryResponse toSummaryResponse(AlumniClass alumniClass) {
        return new AlumniClassSummaryResponse(
                alumniClass.getId(),
                alumniClass.getName(),
                alumniClass.getGraduationYear(),
                alumniClass.getCoverMediaId(),
                alumniClass.getDescription(),
                alumniClass.getStudents() != null ? alumniClass.getStudents().size() : 0,
                toStudentSummaryResponses(alumniClass.getStudents())
        );
    }

    public List<AlumniClassSummaryResponse> toSummaryResponses(List<AlumniClass> classes) {
        if (classes == null || classes.isEmpty()) {
            return List.of();
        }
        return classes.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    public CreateAlumniClassCommand.StudentPayload toStudentPayload(AlumniClassStudentRequest request) {
        return toStudentPayload(null, request);
    }

    public CreateAlumniClassCommand.StudentPayload toStudentPayload(UUID studentId,
                                                                    AlumniClassStudentRequest request) {
        if (request == null) {
            return null;
        }
        Integer displayOrder = request.displayOrder();
        UUID payloadId = studentId != null ? studentId : request.id();
        return new CreateAlumniClassCommand.StudentPayload(
                payloadId,
                request.fullName(),
                request.role(),
                request.quote(),
                request.profile(),
                request.capstoneProjectDescription(),
                request.schoolAttended(),
                request.currentLocation(),
                request.projectWorkedOn(),
                request.photoMediaId(),
                displayOrder
        );
    }

    public AlumniClassStudentResponse toStudentResponse(AlumniClassStudent student) {
        if (student == null) {
            return null;
        }
        return new AlumniClassStudentResponse(
                student.getId(),
                student.getFullName(),
                student.getRole(),
                student.getQuote(),
                student.getProfile(),
                student.getCapstoneProjectDescription(),
                student.getSchoolAttended(),
                student.getCurrentLocation(),
                student.getProjectWorkedOn(),
                student.getPhotoMediaId(),
                student.getDisplayOrder()
        );
    }

    public List<AlumniClassResponse> toResponses(List<AlumniClass> classes) {
        if (classes == null || classes.isEmpty()) {
            return List.of();
        }
        return classes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private List<CreateAlumniClassCommand.StudentPayload> toStudentPayloads(List<AlumniClassStudentRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        List<CreateAlumniClassCommand.StudentPayload> payloads = new ArrayList<>(requests.size());
        for (int index = 0; index < requests.size(); index++) {
            AlumniClassStudentRequest request = requests.get(index);
            Integer displayOrder = request.displayOrder() != null ? request.displayOrder() : index;
            payloads.add(new CreateAlumniClassCommand.StudentPayload(
                    request.id(),
                    request.fullName(),
                    request.role(),
                    request.quote(),
                    request.profile(),
                    request.capstoneProjectDescription(),
                    request.schoolAttended(),
                    request.currentLocation(),
                    request.projectWorkedOn(),
                    request.photoMediaId(),
                    displayOrder
            ));
        }
        return payloads;
    }

    public CreateAlumniClassCommand.GalleryItemPayload toGalleryPayload(AlumniClassGalleryItemRequest request) {
        if (request == null) {
            return null;
        }
        Integer displayOrder = request.displayOrder() != null ? request.displayOrder() : 0;
        return new CreateAlumniClassCommand.GalleryItemPayload(
                request.id(),
                request.mediaId(),
                request.caption(),
                displayOrder
        );
    }

    private List<CreateAlumniClassCommand.GalleryItemPayload> toGalleryPayloads(List<AlumniClassGalleryItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        List<CreateAlumniClassCommand.GalleryItemPayload> payloads = new ArrayList<>(requests.size());
        for (int index = 0; index < requests.size(); index++) {
            AlumniClassGalleryItemRequest request = requests.get(index);
            Integer displayOrder = request.displayOrder() != null ? request.displayOrder() : index;
            payloads.add(new CreateAlumniClassCommand.GalleryItemPayload(
                    request.id(),
                    request.mediaId(),
                    request.caption(),
                    displayOrder
            ));
        }
        return payloads;
    }

    public List<AlumniClassStudentResponse> toStudentResponses(List<AlumniClassStudent> students) {
        if (students == null || students.isEmpty()) {
            return List.of();
        }
        return students.stream()
                .map(this::toStudentResponse)
                .collect(Collectors.toList());
    }

    public AlumniClassGalleryItemResponse toGalleryResponse(AlumniClassGalleryItem item) {
        if (item == null) {
            return null;
        }
        return new AlumniClassGalleryItemResponse(
                item.getId(),
                item.getMediaId(),
                item.getCaption(),
                item.getDisplayOrder()
        );
    }

    public List<AlumniClassGalleryItemResponse> toGalleryResponses(List<AlumniClassGalleryItem> galleryItems) {
        if (galleryItems == null || galleryItems.isEmpty()) {
            return List.of();
        }
        return galleryItems.stream()
                .sorted(Comparator.comparing(AlumniClassGalleryItem::getDisplayOrder))
                .map(this::toGalleryResponse)
                .collect(Collectors.toList());
    }

    private List<AlumniClassStudentSummaryResponse> toStudentSummaryResponses(List<AlumniClassStudent> students) {
        if (students == null || students.isEmpty()) {
            return List.of();
        }
        return students.stream()
                .sorted(Comparator.comparing(AlumniClassStudent::getDisplayOrder))
                .map(student -> new AlumniClassStudentSummaryResponse(
                        student.getId(),
                        student.getFullName(),
                        student.getRole(),
                        summarizeProfile(student.getProfile()),
                        student.getPhotoMediaId(),
                        student.getDisplayOrder()
                ))
                .collect(Collectors.toList());
    }

    private String summarizeProfile(String profile) {
        if (profile == null) {
            return null;
        }
        String normalized = profile.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() <= PROFILE_SUMMARY_MAX_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, PROFILE_SUMMARY_MAX_LENGTH - 1).trim() + "…";
    }

}
