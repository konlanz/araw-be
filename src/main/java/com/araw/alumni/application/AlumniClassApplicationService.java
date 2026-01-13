package com.araw.alumni.application;

import com.araw.alumni.application.command.CreateAlumniClassCommand;
import com.araw.alumni.application.command.UpdateAlumniClassCommand;
import com.araw.alumni.domain.model.AlumniClass;
import com.araw.alumni.domain.model.AlumniClassGalleryItem;
import com.araw.alumni.domain.model.AlumniClassStudent;
import com.araw.alumni.domain.repository.AlumniClassRepository;
import com.araw.media.application.MediaStorageService;
import com.araw.media.application.command.MediaUploadCommand;
import com.araw.media.domain.model.MediaAsset;
import com.araw.media.domain.model.MediaCategory;
import com.araw.shared.exception.DomainNotFoundException;
import com.araw.shared.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AlumniClassApplicationService {

    private final AlumniClassRepository alumniClassRepository;
    private final MediaStorageService mediaStorageService;

    public AlumniClass createClass(CreateAlumniClassCommand command) {
        AlumniClass alumniClass = AlumniClass.create(
                command.name(),
                command.graduationYear(),
                command.description(),
                command.coverMediaId()
        );
        applyStudentPayloads(alumniClass, command.students());
        applyGalleryPayloads(alumniClass, command.galleryItems());
        return alumniClassRepository.save(alumniClass);
    }

    public AlumniClass updateClass(UpdateAlumniClassCommand command) {
        AlumniClass alumniClass = getById(command.id());
        alumniClass.updateDetails(
                command.name(),
                command.graduationYear(),
                command.description(),
                command.coverMediaId()
        );
        applyStudentPayloads(alumniClass, command.students());
        applyGalleryPayloads(alumniClass, command.galleryItems());
        return alumniClassRepository.save(alumniClass);
    }

    public void deleteClass(UUID classId) {
        AlumniClass alumniClass = getById(classId);
        alumniClassRepository.delete(alumniClass);
    }

    @Transactional(readOnly = true)
    public AlumniClass getById(UUID classId) {
        AlumniClass alumniClass = alumniClassRepository.findById(classId)
                .orElseThrow(() -> new DomainNotFoundException("Alumni class not found: " + classId));
        initializeDetails(alumniClass);
        return alumniClass;
    }

    @Transactional(readOnly = true)
    public Page<AlumniClass> findAll(Pageable pageable) {
        Page<AlumniClass> page = alumniClassRepository.findAll(pageable);
        page.forEach(this::initializeDetails);
        return page;
    }

    @Transactional(readOnly = true)
    public List<AlumniClass> findAllOrderedByGraduationYear() {
        List<AlumniClass> classes = alumniClassRepository.findAllOrderedByGraduationYearDesc();
        classes.forEach(this::initializeDetails);
        return classes;
    }

    public AlumniClassStudent addStudent(UUID classId, CreateAlumniClassCommand.StudentPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Student payload is required");
        }
        if (payload.id() != null) {
            throw new IllegalArgumentException("Student id must be null when adding a new student");
        }

        AlumniClass alumniClass = getById(classId);
        int nextOrder = alumniClass.getStudents().stream()
                .map(AlumniClassStudent::getDisplayOrder)
                .max(Integer::compareTo)
                .map(order -> order + 1)
                .orElse(0);

        Integer displayOrder = payload.displayOrder() != null ? payload.displayOrder() : nextOrder;

        AlumniClassStudent student = AlumniClassStudent.create(
                payload.fullName(),
                payload.role(),
                payload.quote(),
                payload.profile(),
                payload.capstoneProjectDescription(),
                payload.schoolAttended(),
                payload.currentLocation(),
                payload.projectWorkedOn(),
                payload.photoMediaId(),
                displayOrder
        );

        alumniClass.addStudent(student);
        student.setDisplayOrder(displayOrder);
        reorderStudentsByDisplayOrder(alumniClass);
        alumniClassRepository.save(alumniClass);
        return student;
    }

    public AlumniClassStudent addStudentWithPhoto(UUID classId,
                                                  CreateAlumniClassCommand.StudentPayload payload,
                                                  MultipartFile photoFile) {
        if (payload == null) {
            throw new IllegalArgumentException("Student payload is required");
        }

        UUID photoMediaId = payload.photoMediaId();
        if (photoFile != null && !photoFile.isEmpty()) {
            photoMediaId = storeStudentPhoto(photoFile, payload.fullName());
        }

        CreateAlumniClassCommand.StudentPayload payloadWithPhoto = new CreateAlumniClassCommand.StudentPayload(
                payload.id(),
                payload.fullName(),
                payload.role(),
                payload.quote(),
                payload.profile(),
                payload.capstoneProjectDescription(),
                payload.schoolAttended(),
                payload.currentLocation(),
                payload.projectWorkedOn(),
                photoMediaId,
                payload.displayOrder()
        );

        return addStudent(classId, payloadWithPhoto);
    }

    public AlumniClassGalleryItem addGalleryItem(UUID classId,
                                                 CreateAlumniClassCommand.GalleryItemPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Gallery payload is required");
        }
        if (payload.id() != null) {
            throw new IllegalArgumentException("Gallery item id must be null when adding a new item");
        }
        AlumniClass alumniClass = getById(classId);

        int nextOrder = alumniClass.getGalleryItems().stream()
                .map(AlumniClassGalleryItem::getDisplayOrder)
                .max(Integer::compareTo)
                .map(order -> order + 1)
                .orElse(0);

        Integer displayOrder = payload.displayOrder() != null ? payload.displayOrder() : nextOrder;

        AlumniClassGalleryItem item = AlumniClassGalleryItem.create(
                payload.mediaId(),
                payload.caption(),
                displayOrder
        );

        alumniClass.addGalleryItem(item);
        item.setDisplayOrder(displayOrder);
        alumniClass.reorderGalleryItems(alumniClass.getGalleryItems().stream()
                .sorted(Comparator.comparing(AlumniClassGalleryItem::getDisplayOrder))
                .collect(Collectors.toList()));
        alumniClassRepository.save(alumniClass);
        return item;
    }

    public AlumniClassGalleryItem addGalleryItemWithMedia(UUID classId,
                                                         String caption,
                                                         Integer displayOrder,
                                                         MultipartFile mediaFile) {
        if (mediaFile == null || mediaFile.isEmpty()) {
            throw new DomainValidationException("Gallery media file is required");
        }
        UUID mediaId = storeGalleryMedia(mediaFile, caption);
        CreateAlumniClassCommand.GalleryItemPayload payload = new CreateAlumniClassCommand.GalleryItemPayload(
                null,
                mediaId,
                caption,
                displayOrder
        );
        return addGalleryItem(classId, payload);
    }

    public AlumniClassStudent updateStudent(UUID classId,
                                            UUID studentId,
                                            CreateAlumniClassCommand.StudentPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Student payload is required");
        }

        AlumniClass alumniClass = getById(classId);
        AlumniClassStudent student = findStudentOrThrow(alumniClass, studentId);

        if (payload.id() != null && !studentId.equals(payload.id())) {
            throw new DomainValidationException("Student id mismatch for update: " + studentId);
        }

        student.updateDetails(
                payload.fullName(),
                payload.role(),
                payload.quote(),
                payload.profile(),
                payload.capstoneProjectDescription(),
                payload.schoolAttended(),
                payload.currentLocation(),
                payload.projectWorkedOn(),
                payload.photoMediaId()
        );

        if (payload.displayOrder() != null && !payload.displayOrder().equals(student.getDisplayOrder())) {
            student.setDisplayOrder(payload.displayOrder());
            reorderStudentsByDisplayOrder(alumniClass);
        }

        alumniClassRepository.save(alumniClass);
        return student;
    }

    public AlumniClassStudent updateStudentWithPhoto(UUID classId,
                                                     UUID studentId,
                                                     CreateAlumniClassCommand.StudentPayload payload,
                                                     MultipartFile photoFile) {
        if (payload == null) {
            throw new IllegalArgumentException("Student payload is required");
        }

        UUID photoMediaId = payload.photoMediaId();
        if (photoFile != null && !photoFile.isEmpty()) {
            photoMediaId = storeStudentPhoto(photoFile, payload.fullName());
        }

        CreateAlumniClassCommand.StudentPayload payloadWithPhoto = new CreateAlumniClassCommand.StudentPayload(
                studentId,
                payload.fullName(),
                payload.role(),
                payload.quote(),
                payload.profile(),
                payload.capstoneProjectDescription(),
                payload.schoolAttended(),
                payload.currentLocation(),
                payload.projectWorkedOn(),
                photoMediaId,
                payload.displayOrder()
        );

        return updateStudent(classId, studentId, payloadWithPhoto);
    }

    public AlumniClassGalleryItem updateGalleryItem(UUID classId,
                                                    UUID itemId,
                                                    CreateAlumniClassCommand.GalleryItemPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Gallery payload is required");
        }
        AlumniClass alumniClass = getById(classId);
        AlumniClassGalleryItem item = findGalleryItemOrThrow(alumniClass, itemId);

        if (payload.id() != null && !itemId.equals(payload.id())) {
            throw new DomainValidationException("Gallery item id mismatch for update: " + itemId);
        }

        item.updateDetails(payload.mediaId(), payload.caption());
        if (payload.displayOrder() != null && !payload.displayOrder().equals(item.getDisplayOrder())) {
            item.setDisplayOrder(payload.displayOrder());
            alumniClass.reorderGalleryItems(alumniClass.getGalleryItems().stream()
                    .sorted(Comparator.comparing(AlumniClassGalleryItem::getDisplayOrder))
                    .collect(Collectors.toList()));
        }

        alumniClassRepository.save(alumniClass);
        return item;
    }

    public AlumniClassGalleryItem updateGalleryItemWithMedia(UUID classId,
                                                             UUID itemId,
                                                             String caption,
                                                             Integer displayOrder,
                                                             MultipartFile mediaFile) {
        if (mediaFile == null || mediaFile.isEmpty()) {
            throw new DomainValidationException("Gallery media file is required");
        }
        UUID mediaId = storeGalleryMedia(mediaFile, caption);
        CreateAlumniClassCommand.GalleryItemPayload payload = new CreateAlumniClassCommand.GalleryItemPayload(
                itemId,
                mediaId,
                caption,
                displayOrder
        );
        return updateGalleryItem(classId, itemId, payload);
    }

    @Transactional(readOnly = true)
    public List<AlumniClassStudent> listStudents(UUID classId) {
        AlumniClass alumniClass = getById(classId);
        return List.copyOf(alumniClass.getStudents());
    }

    private void applyStudentPayloads(AlumniClass alumniClass,
                                      List<CreateAlumniClassCommand.StudentPayload> studentPayloads) {
        List<CreateAlumniClassCommand.StudentPayload> safePayloads = studentPayloads != null
                ? studentPayloads
                : List.of();

        Map<UUID, AlumniClassStudent> existingById = alumniClass.getStudents().stream()
                .filter(student -> student.getId() != null)
                .collect(Collectors.toMap(AlumniClassStudent::getId, student -> student));

        Set<AlumniClassStudent> retained = Collections.newSetFromMap(new IdentityHashMap<>());
        List<AlumniClassStudent> orderedStudents = new ArrayList<>();

        int position = 0;
        for (CreateAlumniClassCommand.StudentPayload payload : safePayloads) {
            int fallbackOrder = position++;
            Integer displayOrder = payload.displayOrder() != null ? payload.displayOrder() : fallbackOrder;

            AlumniClassStudent student = payload.id() != null
                    ? existingById.get(payload.id())
                    : null;

            if (student != null) {
                student.updateDetails(
                        payload.fullName(),
                        payload.role(),
                        payload.quote(),
                        payload.profile(),
                        payload.capstoneProjectDescription(),
                        payload.schoolAttended(),
                        payload.currentLocation(),
                        payload.projectWorkedOn(),
                        payload.photoMediaId()
                );
            } else {
                student = AlumniClassStudent.create(
                        payload.fullName(),
                        payload.role(),
                        payload.quote(),
                        payload.profile(),
                        payload.capstoneProjectDescription(),
                        payload.schoolAttended(),
                        payload.currentLocation(),
                        payload.projectWorkedOn(),
                        payload.photoMediaId(),
                        displayOrder
                );
                alumniClass.addStudent(student);
            }

            student.setDisplayOrder(displayOrder);
            retained.add(student);
            orderedStudents.add(student);
        }

        List<AlumniClassStudent> toRemove = alumniClass.getStudents().stream()
                .filter(student -> !retained.contains(student))
                .collect(Collectors.toList());
        toRemove.forEach(alumniClass::removeStudent);

        orderedStudents.sort(Comparator.comparing(AlumniClassStudent::getDisplayOrder));
        alumniClass.reorderStudents(orderedStudents);
    }

    private void applyGalleryPayloads(AlumniClass alumniClass,
                                      List<CreateAlumniClassCommand.GalleryItemPayload> galleryPayloads) {
        List<CreateAlumniClassCommand.GalleryItemPayload> safePayloads = galleryPayloads != null
                ? galleryPayloads
                : List.of();

        Map<UUID, AlumniClassGalleryItem> existingById = alumniClass.getGalleryItems().stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(AlumniClassGalleryItem::getId, item -> item));

        Set<AlumniClassGalleryItem> retained = Collections.newSetFromMap(new IdentityHashMap<>());
        List<AlumniClassGalleryItem> orderedItems = new ArrayList<>();

        int position = 0;
        for (CreateAlumniClassCommand.GalleryItemPayload payload : safePayloads) {
            int fallbackOrder = position++;
            Integer displayOrder = payload.displayOrder() != null ? payload.displayOrder() : fallbackOrder;

            AlumniClassGalleryItem galleryItem = payload.id() != null
                    ? existingById.get(payload.id())
                    : null;

            if (galleryItem != null) {
                galleryItem.updateDetails(
                        payload.mediaId(),
                        payload.caption()
                );
            } else {
                galleryItem = AlumniClassGalleryItem.create(
                        payload.mediaId(),
                        payload.caption(),
                        displayOrder
                );
                alumniClass.addGalleryItem(galleryItem);
            }

            galleryItem.setDisplayOrder(displayOrder);
            retained.add(galleryItem);
            orderedItems.add(galleryItem);
        }

        List<AlumniClassGalleryItem> toRemove = alumniClass.getGalleryItems().stream()
                .filter(item -> !retained.contains(item))
                .collect(Collectors.toList());
        toRemove.forEach(alumniClass::removeGalleryItem);

        alumniClass.reorderGalleryItems(orderedItems);
    }

    private void initializeDetails(AlumniClass alumniClass) {
        if (alumniClass == null) {
            return;
        }

        alumniClass.getStudents().size();
        alumniClass.getGalleryItems().size();
    }

    private AlumniClassStudent findStudentOrThrow(AlumniClass alumniClass, UUID studentId) {
        return alumniClass.getStudents().stream()
                .filter(student -> studentId.equals(student.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainNotFoundException("Student not found in class: " + studentId));
    }

    private AlumniClassGalleryItem findGalleryItemOrThrow(AlumniClass alumniClass, UUID itemId) {
        return alumniClass.getGalleryItems().stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainNotFoundException("Gallery item not found in class: " + itemId));
    }

    private void reorderStudentsByDisplayOrder(AlumniClass alumniClass) {
        List<AlumniClassStudent> ordered = alumniClass.getStudents().stream()
                .sorted(Comparator.comparing(AlumniClassStudent::getDisplayOrder))
                .collect(Collectors.toList());
        alumniClass.reorderStudents(ordered);
    }

    private UUID storeStudentPhoto(MultipartFile photoFile, String studentName) {
        if (photoFile.isEmpty()) {
            throw new DomainValidationException("Uploaded photo file cannot be empty");
        }

        String description = "Alumni student photo";
        if (studentName != null && !studentName.isBlank()) {
            description = "Alumni student photo for " + studentName.trim();
        }

        try (MediaUploadCommand command = new MediaUploadCommand(
                photoFile.getOriginalFilename(),
                photoFile.getContentType(),
                photoFile.getSize(),
                photoFile.getInputStream(),
                MediaCategory.PROFILE_PHOTO,
                description
        )) {
            MediaAsset asset = mediaStorageService.storeMedia(command);
            return asset.getId();
        } catch (IOException ex) {
            throw new DomainValidationException("Failed to read uploaded photo", ex);
        }
    }

    private UUID storeGalleryMedia(MultipartFile mediaFile, String caption) {
        if (mediaFile.isEmpty()) {
            throw new DomainValidationException("Uploaded gallery file cannot be empty");
        }
        String description = "Alumni gallery item";
        if (caption != null && !caption.isBlank()) {
            description = "Alumni gallery item - " + caption.trim();
        }
        try (MediaUploadCommand command = new MediaUploadCommand(
                mediaFile.getOriginalFilename(),
                mediaFile.getContentType(),
                mediaFile.getSize(),
                mediaFile.getInputStream(),
                MediaCategory.EVENT_GALLERY_IMAGE,
                description
        )) {
            MediaAsset asset = mediaStorageService.storeMedia(command);
            return asset.getId();
        } catch (IOException ex) {
            throw new DomainValidationException("Failed to read uploaded gallery file", ex);
        }
    }
}
