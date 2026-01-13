package com.araw.alumni.presentation;

import com.araw.alumni.application.AlumniClassApplicationService;
import com.araw.alumni.presentation.dto.AlumniClassGalleryItemRequest;
import com.araw.alumni.presentation.dto.AlumniClassGalleryItemResponse;
import com.araw.alumni.presentation.dto.AlumniClassResponse;
import com.araw.alumni.presentation.dto.AlumniClassSummaryResponse;
import com.araw.alumni.presentation.dto.AlumniClassStudentRequest;
import com.araw.alumni.presentation.dto.AlumniClassStudentResponse;
import com.araw.alumni.presentation.dto.CreateAlumniClassRequest;
import com.araw.alumni.presentation.dto.UpdateAlumniClassRequest;
import com.araw.alumni.presentation.mapper.AlumniClassMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alumni/classes")
@RequiredArgsConstructor
public class AlumniClassController {

    private final AlumniClassApplicationService alumniClassService;
    private final AlumniClassMapper mapper;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final jakarta.validation.Validator validator;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlumniClassResponse createAlumniClass(@Valid @RequestBody CreateAlumniClassRequest request) {
        var command = mapper.toCreateCommand(request);
        var alumniClass = alumniClassService.createClass(command);
        return mapper.toResponse(alumniClass);
    }

    @PutMapping("/{classId}")
    public AlumniClassResponse updateAlumniClass(@PathVariable UUID classId,
                                                 @Valid @RequestBody UpdateAlumniClassRequest request) {
        var command = mapper.toUpdateCommand(classId, request);
        var alumniClass = alumniClassService.updateClass(command);
        return mapper.toResponse(alumniClass);
    }

    @PostMapping(value = "/{classId}/students", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AlumniClassStudentResponse addStudent(@PathVariable UUID classId,
                                                 @Valid @RequestBody AlumniClassStudentRequest request) {
        var payload = mapper.toStudentPayload(request);
        var student = alumniClassService.addStudent(classId, payload);
        return mapper.toStudentResponse(student);
    }

    @PostMapping(value = "/{classId}/students", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AlumniClassStudentResponse addStudentWithPhoto(@PathVariable UUID classId,
                                                          @RequestPart("payload") String payloadJson,
                                                          @RequestPart(value = "photo", required = false) MultipartFile photo) {
        var request = parseStudentRequest(payloadJson);
        var payload = mapper.toStudentPayload(request);
        var student = alumniClassService.addStudentWithPhoto(classId, payload, photo);
        return mapper.toStudentResponse(student);
    }

    @PutMapping(value = "/{classId}/students/{studentId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AlumniClassStudentResponse updateStudent(@PathVariable UUID classId,
                                                    @PathVariable UUID studentId,
                                                    @Valid @RequestBody AlumniClassStudentRequest request) {
        var payload = mapper.toStudentPayload(studentId, request);
        var student = alumniClassService.updateStudent(classId, studentId, payload);
        return mapper.toStudentResponse(student);
    }

    @PutMapping(value = "/{classId}/students/{studentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AlumniClassStudentResponse updateStudentWithPhoto(@PathVariable UUID classId,
                                                             @PathVariable UUID studentId,
                                                             @RequestPart("payload") String payloadJson,
                                                             @RequestPart(value = "photo", required = false) MultipartFile photo) {
        var request = parseStudentRequest(payloadJson);
        var payload = mapper.toStudentPayload(studentId, request);
        var student = alumniClassService.updateStudentWithPhoto(classId, studentId, payload, photo);
        return mapper.toStudentResponse(student);
    }

    @PostMapping(value = "/{classId}/gallery", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AlumniClassGalleryItemResponse addGalleryItem(@PathVariable UUID classId,
                                                         @Valid @RequestBody AlumniClassGalleryItemRequest request) {
        var payload = mapper.toGalleryPayload(request);
        var item = alumniClassService.addGalleryItem(classId, payload);
        return mapper.toGalleryResponse(item);
    }

    @PostMapping(value = "/{classId}/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AlumniClassGalleryItemResponse addGalleryItemWithMedia(@PathVariable UUID classId,
                                                                  @RequestPart(value = "caption", required = false) String caption,
                                                                  @RequestPart(value = "displayOrder", required = false) Integer displayOrder,
                                                                  @RequestPart("media") MultipartFile media) {
        var item = alumniClassService.addGalleryItemWithMedia(classId, caption, displayOrder, media);
        return mapper.toGalleryResponse(item);
    }

    @PutMapping(value = "/{classId}/gallery/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AlumniClassGalleryItemResponse updateGalleryItem(@PathVariable UUID classId,
                                                            @PathVariable UUID itemId,
                                                            @Valid @RequestBody AlumniClassGalleryItemRequest request) {
        var payload = mapper.toGalleryPayload(request);
        var item = alumniClassService.updateGalleryItem(classId, itemId, payload);
        return mapper.toGalleryResponse(item);
    }

    @PutMapping(value = "/{classId}/gallery/{itemId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AlumniClassGalleryItemResponse updateGalleryItemWithMedia(@PathVariable UUID classId,
                                                                     @PathVariable UUID itemId,
                                                                     @RequestPart(value = "caption", required = false) String caption,
                                                                     @RequestPart(value = "displayOrder", required = false) Integer displayOrder,
                                                                     @RequestPart("media") MultipartFile media) {
        var item = alumniClassService.updateGalleryItemWithMedia(classId, itemId, caption, displayOrder, media);
        return mapper.toGalleryResponse(item);
    }

    @GetMapping("/{classId}/students")
    public List<AlumniClassStudentResponse> listStudents(@PathVariable UUID classId) {
        var students = alumniClassService.listStudents(classId);
        return mapper.toStudentResponses(students);
    }

    @DeleteMapping("/{classId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAlumniClass(@PathVariable UUID classId) {
        alumniClassService.deleteClass(classId);
    }

    @GetMapping
    public List<AlumniClassResponse> listAlumniClasses() {
        var classes = alumniClassService.findAllOrderedByGraduationYear();
        return mapper.toResponses(classes);
    }

    @GetMapping("/summary")
    public List<AlumniClassSummaryResponse> listAlumniClassSummaries() {
        var classes = alumniClassService.findAllOrderedByGraduationYear();
        return mapper.toSummaryResponses(classes);
    }

    @GetMapping("/{classId}/summary")
    public AlumniClassSummaryResponse getAlumniClassSummary(@PathVariable UUID classId) {
        var alumniClass = alumniClassService.getById(classId);
        return mapper.toSummaryResponse(alumniClass);
    }

    @GetMapping("/{classId}")
    public AlumniClassResponse getAlumniClass(@PathVariable UUID classId) {
        var alumniClass = alumniClassService.getById(classId);
        return mapper.toResponse(alumniClass);
    }

    private AlumniClassStudentRequest parseStudentRequest(String payloadJson) {
        try {
            var request = objectMapper.readValue(payloadJson, AlumniClassStudentRequest.class);
            var violations = validator.validate(request);
            if (!violations.isEmpty()) {
                throw new jakarta.validation.ConstraintViolationException(violations);
            }
            return request;
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException("Invalid student payload JSON", ex);
        }
    }
}
