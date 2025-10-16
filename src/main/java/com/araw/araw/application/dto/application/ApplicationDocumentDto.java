package com.araw.araw.application.dto.application;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDocumentDto {
    private UUID id;
    private String documentType;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime uploadedAt;
}
