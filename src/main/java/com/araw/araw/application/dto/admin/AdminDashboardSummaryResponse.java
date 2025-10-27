package com.araw.araw.application.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class AdminDashboardSummaryResponse {
    private long totalEvents;
    private Map<String, Long> eventsByStatus;
    private Map<String, Long> eventsByType;
    private long totalArticles;
    private Map<String, Long> articlesByStatus;
    private long totalMediaAssets;
    private Map<String, Long> mediaAssetsByCategory;
    private long totalParticipantsServed;
    private double averageParticipantsPerEvent;
    private double averageCapacityUtilization;
}
