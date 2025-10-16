package com.araw.araw.application.dto.event;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelEventRequest {

    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}
