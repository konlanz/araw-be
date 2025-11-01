package com.araw.araw.application.mapper;

import com.araw.araw.application.dto.application.ApplicantInfoDto;
import com.araw.araw.application.dto.application.ApplicationDocumentDto;
import com.araw.araw.application.dto.application.ApplicationResponse;
import com.araw.araw.application.dto.application.CreateApplicationRequest;
import com.araw.araw.application.dto.application.UpdateApplicationRequest;
import com.araw.araw.domain.application.entity.Application;
import com.araw.araw.domain.application.entity.ApplicationDocument;
import com.araw.araw.domain.application.valueobject.ApplicantInfo;
import com.araw.araw.domain.application.valueobject.ApplicationStatus;
import org.mapstruct.*;

import java.util.List;
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ApplicationMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    @Mapping(target = "participantId", source = "participant.id")
    @Mapping(target = "customAnswers", expression = "java(copyMap(application.getCustomAnswers()))")
    @Mapping(target = "dietaryRestrictions", expression = "java(copySet(application.getDietaryRestrictions()))")
    @Mapping(target = "medicalConditions", expression = "java(copySet(application.getMedicalConditions()))")
    ApplicationResponse toResponse(Application application);

    List<ApplicationResponse> toResponseList(List<Application> applications);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "participant", ignore = true)
    @Mapping(target = "status", expression = "java(initialStatus())")
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewNotes", ignore = true)
    @Mapping(target = "waitlistPosition", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "applicantInfo", expression = "java(toApplicantInfo(request.getApplicantInfo()))")
    Application toEntity(CreateApplicationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "participant", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "waitlistPosition", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "applicantInfo", expression = "java(updateApplicantInfo(application.getApplicantInfo(), request.getApplicantInfo()))")
    void updateEntity(@MappingTarget Application application, UpdateApplicationRequest request);

    default ApplicationStatus initialStatus() {
        return ApplicationStatus.DRAFT;
    }

    default ApplicantInfo toApplicantInfo(ApplicantInfoDto dto) {
        if (dto == null) {
            return null;
        }
        return ApplicantInfo.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .middleName(dto.getMiddleName())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .ethnicity(dto.getEthnicity())
                .phoneNumber(dto.getPhoneNumber())
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity())
                .stateProvince(dto.getStateProvince())
                .postalCode(dto.getPostalCode())
                .country(dto.getCountry())
                .schoolName(dto.getSchoolName())
                .gradeLevel(dto.getGradeLevel())
                .gpa(dto.getGpa())
                .preferredLanguage(dto.getPreferredLanguage())
                .build();
    }

    default ApplicantInfo updateApplicantInfo(ApplicantInfo existing, ApplicantInfoDto dto) {
        if (dto == null) {
            return existing;
        }
        ApplicantInfo target = existing != null ? existing : new ApplicantInfo();
        if (dto.getFirstName() != null) target.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) target.setLastName(dto.getLastName());
        if (dto.getMiddleName() != null) target.setMiddleName(dto.getMiddleName());
        if (dto.getDateOfBirth() != null) target.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) target.setGender(dto.getGender());
        if (dto.getEthnicity() != null) target.setEthnicity(dto.getEthnicity());
        if (dto.getPhoneNumber() != null) target.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getAddressLine1() != null) target.setAddressLine1(dto.getAddressLine1());
        if (dto.getAddressLine2() != null) target.setAddressLine2(dto.getAddressLine2());
        if (dto.getCity() != null) target.setCity(dto.getCity());
        if (dto.getStateProvince() != null) target.setStateProvince(dto.getStateProvince());
        if (dto.getPostalCode() != null) target.setPostalCode(dto.getPostalCode());
        if (dto.getCountry() != null) target.setCountry(dto.getCountry());
        if (dto.getSchoolName() != null) target.setSchoolName(dto.getSchoolName());
        if (dto.getGradeLevel() != null) target.setGradeLevel(dto.getGradeLevel());
        if (dto.getGpa() != null) target.setGpa(dto.getGpa());
        if (dto.getPreferredLanguage() != null) target.setPreferredLanguage(dto.getPreferredLanguage());
        return target;
    }

    default ApplicationDocumentDto toDocumentDto(ApplicationDocument document) {
        if (document == null) {
            return null;
        }
        return ApplicationDocumentDto.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .fileSize(document.getFileSize())
                .mimeType(document.getMimeType())
                .mediaAssetId(document.getMediaAssetId())
                .uploadedAt(document.getUploadedAt())
                .build();
    }

    default java.util.Map<String, String> copyMap(java.util.Map<String, String> source) {
        return source == null ? null : new java.util.LinkedHashMap<>(source);
    }

    default java.util.Set<String> copySet(java.util.Set<String> source) {
        return source == null ? null : new java.util.LinkedHashSet<>(source);
    }
}
