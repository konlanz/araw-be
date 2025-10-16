package com.araw.araw.application.dto.application;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDecisionRequest {

    @NotBlank(message = "Reason is required")
    private String reason;
}
