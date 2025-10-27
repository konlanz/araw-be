package com.araw.araw.presentation;

import com.araw.araw.application.dto.application.ApplicationResponse;
import com.araw.araw.application.dto.publicapp.PublicEventApplicationRequest;
import com.araw.araw.application.service.PublicEventApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/events")
@RequiredArgsConstructor
public class PublicEventApplicationController {

    private final PublicEventApplicationService publicEventApplicationService;

    @PostMapping("/{applicationSlug}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse submitApplication(@PathVariable String applicationSlug,
                                                 @Valid @RequestBody PublicEventApplicationRequest request) {
        return publicEventApplicationService.submitApplication(applicationSlug, request);
    }
}
