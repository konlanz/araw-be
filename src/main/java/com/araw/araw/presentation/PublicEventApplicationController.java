package com.araw.araw.presentation;

import com.araw.araw.application.dto.application.ApplicationResponse;
import com.araw.araw.application.dto.publicapp.PublicEventApplicationRequest;
import com.araw.araw.application.service.PublicEventApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/events")
@RequiredArgsConstructor
public class PublicEventApplicationController {

    private final PublicEventApplicationService publicEventApplicationService;

    @PostMapping(value = "/{applicationSlug}/applications", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse submitApplication(@PathVariable String applicationSlug,
                                                 @Valid @RequestBody PublicEventApplicationRequest request) {
        return publicEventApplicationService.submitApplication(applicationSlug, request);
    }

    @PostMapping(value = "/id/{eventId}/applications", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse submitApplicationByEventId(@PathVariable UUID eventId,
                                                          @Valid @RequestBody PublicEventApplicationRequest request) {
        return publicEventApplicationService.submitApplication(eventId, request);
    }

    @PostMapping(value = "/{applicationSlug}/applications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse submitApplicationWithResume(@PathVariable String applicationSlug,
                                                           @Valid @RequestPart("payload") PublicEventApplicationRequest request,
                                                           @RequestPart(value = "resume", required = false) MultipartFile resume) {
        return publicEventApplicationService.submitApplication(applicationSlug, request, resume);
    }

    @PostMapping(value = "/id/{eventId}/applications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse submitApplicationByEventIdWithResume(@PathVariable UUID eventId,
                                                                    @Valid @RequestPart("payload") PublicEventApplicationRequest request,
                                                                    @RequestPart(value = "resume", required = false) MultipartFile resume) {
        return publicEventApplicationService.submitApplication(eventId, request, resume);
    }
}
