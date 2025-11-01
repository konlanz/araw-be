package com.araw.araw.presentation;

import com.araw.araw.application.dto.application.ApplicationReviewNoteResponse;
import com.araw.araw.application.dto.application.CreateApplicationReviewNoteRequest;
import com.araw.araw.application.service.ApplicationReviewNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/applications")
@RequiredArgsConstructor
public class ApplicationReviewNoteController {

    private final ApplicationReviewNoteService reviewNoteService;

    @PostMapping("/{applicationId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationReviewNoteResponse addReviewNote(@PathVariable UUID applicationId,
                                                       @Valid @RequestBody CreateApplicationReviewNoteRequest request) {
        return reviewNoteService.addReviewNote(applicationId, request);
    }

    @GetMapping("/{applicationId}/reviews")
    public List<ApplicationReviewNoteResponse> listReviewNotes(@PathVariable UUID applicationId) {
        return reviewNoteService.getReviewNotes(applicationId);
    }
}
