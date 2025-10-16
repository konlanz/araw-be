package com.araw.araw.application.dto.participant;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantSummaryResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Integer age;
    private String grade;
}

