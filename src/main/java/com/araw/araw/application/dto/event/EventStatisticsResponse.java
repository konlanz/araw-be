package com.araw.araw.application.dto.event;

import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventStatisticsResponse {
    private Long totalEvents;
    private Long upcomingEvents;
    private Long completedEvents;
    private Long totalParticipantsServed;
    private Double averageParticipantsPerEvent;
    private Double averageCapacityUtilization;
    private Map<String, Long> eventsByType;
    private Map<String, Long> eventsByStatus;
    private Map<String, Long> eventsByCity;
    private Long virtualEvents;
    private Long inPersonEvents;
    private Long hybridEvents;
}
