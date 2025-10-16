package com.araw.araw.presentation;

import com.araw.araw.application.dto.participant.CreateParticipantRequest;
import com.araw.araw.application.dto.participant.ParticipantResponse;
import com.araw.araw.application.dto.participant.ParticipantSummaryResponse;
import com.araw.araw.application.dto.participant.UpdateParticipantRequest;
import com.araw.araw.application.service.ParticipantApplicationService;
import com.araw.shared.api.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/araw/participants")
@Validated
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantApplicationService participantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipantResponse createParticipant(@Valid @RequestBody CreateParticipantRequest request) {
        return participantService.createParticipant(request);
    }

    @PutMapping("/{participantId}")
    public ParticipantResponse updateParticipant(@PathVariable UUID participantId,
                                                 @Valid @RequestBody UpdateParticipantRequest request) {
        return participantService.updateParticipant(participantId, request);
    }

    @GetMapping("/{participantId}")
    public ParticipantResponse getParticipant(@PathVariable UUID participantId) {
        return participantService.getParticipant(participantId);
    }

    @GetMapping("/code/{participantCode}")
    public ParticipantResponse getParticipantByCode(@PathVariable String participantCode) {
        return participantService.getParticipantByCode(participantCode);
    }

    @GetMapping
    public PagedResponse<ParticipantSummaryResponse> listParticipants(
            @RequestParam(value = "search", required = false) String search,
            Pageable pageable) {
        Page<ParticipantSummaryResponse> page = participantService.listParticipants(search, pageable);
        return PagedResponse.fromPage(page);
    }

    @DeleteMapping("/{participantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteParticipant(@PathVariable UUID participantId) {
        participantService.deleteParticipant(participantId);
    }
}
