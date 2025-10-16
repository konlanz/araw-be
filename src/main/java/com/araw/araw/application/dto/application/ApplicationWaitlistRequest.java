package com.araw.araw.application.dto.application;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationWaitlistRequest {

    @Min(value = 1, message = "Position must be positive")
    private Integer position;
}
