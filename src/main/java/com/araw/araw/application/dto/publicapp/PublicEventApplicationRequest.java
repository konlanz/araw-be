package com.araw.araw.application.dto.publicapp;

import com.araw.araw.application.dto.application.CreateApplicationRequest;
import com.araw.araw.application.dto.participant.CreateParticipantRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicEventApplicationRequest {

    @Valid
    private CreateParticipantRequest participant;

    @Valid
    @NotNull
    private CreateApplicationRequest application;
}
