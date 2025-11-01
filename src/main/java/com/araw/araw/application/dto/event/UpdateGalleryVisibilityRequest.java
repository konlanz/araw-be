package com.araw.araw.application.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGalleryVisibilityRequest {

    @NotNull(message = "Visibility flag is required")
    private Boolean isPublic;
}
