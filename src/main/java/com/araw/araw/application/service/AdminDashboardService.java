package com.araw.araw.application.service;

import com.araw.araw.application.dto.admin.AdminDashboardSummaryResponse;
import com.araw.araw.domain.event.repository.EventRepository;
import com.araw.araw.domain.event.valueobject.EventStatus;
import com.araw.araw.domain.event.valueobject.EventType;
import com.araw.content.domain.model.ArticleStatus;
import com.araw.content.domain.repository.ArticleRepository;
import com.araw.media.domain.model.MediaCategory;
import com.araw.media.domain.repository.MediaAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminDashboardService {

    private final EventRepository eventRepository;
    private final ArticleRepository articleRepository;
    private final MediaAssetRepository mediaAssetRepository;

    public AdminDashboardSummaryResponse getSummary() {
        long totalEvents = eventRepository.count();
        Map<String, Long> eventsByStatus = buildEventStatusCounts();
        Map<String, Long> eventsByType = buildEventTypeCounts();

        Long participantsServed = eventRepository.getTotalParticipantsServed();
        Double avgParticipants = eventRepository.getAverageParticipantsPerEvent();
        Double avgCapacity = eventRepository.getAverageCapacityUtilization();

        long totalParticipantsServed = participantsServed != null ? participantsServed : 0L;
        double averageParticipantsPerEvent = avgParticipants != null ? avgParticipants : 0d;
        double averageCapacityUtilization = avgCapacity != null ? avgCapacity : 0d;

        Map<String, Long> articlesByStatus = buildArticleStatusCounts();
        long totalArticles = articleRepository.count();

        Map<String, Long> mediaByCategory = buildMediaCategoryCounts();
        long totalMediaAssets = mediaByCategory.values().stream().mapToLong(Long::longValue).sum();

        return AdminDashboardSummaryResponse.builder()
                .totalEvents(totalEvents)
                .eventsByStatus(eventsByStatus)
                .eventsByType(eventsByType)
                .totalParticipantsServed(totalParticipantsServed)
                .averageParticipantsPerEvent(averageParticipantsPerEvent)
                .averageCapacityUtilization(averageCapacityUtilization)
                .totalArticles(totalArticles)
                .articlesByStatus(articlesByStatus)
                .totalMediaAssets(totalMediaAssets)
                .mediaAssetsByCategory(mediaByCategory)
                .build();
    }

    private Map<String, Long> buildEventStatusCounts() {
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (EventStatus status : EventStatus.values()) {
            long count = eventRepository.countByStatus(status);
            statusCounts.put(status.name(), count);
        }
        return statusCounts;
    }

    private Map<String, Long> buildEventTypeCounts() {
        Map<String, Long> typeCounts = new LinkedHashMap<>();
        for (EventType type : EventType.values()) {
            typeCounts.put(type.name(), 0L);
        }

        List<Object[]> rawCounts = eventRepository.countEventsByType();
        for (Object[] row : rawCounts) {
            if (row == null || row.length < 2) {
                continue;
            }
            EventType type = (EventType) row[0];
            Long count = (Long) row[1];
            if (type != null) {
                typeCounts.put(type.name(), count != null ? count : 0L);
            }
        }
        return typeCounts;
    }

    private Map<String, Long> buildArticleStatusCounts() {
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (ArticleStatus status : ArticleStatus.values()) {
            long count = articleRepository.countByStatus(status);
            statusCounts.put(status.name(), count);
        }
        return statusCounts;
    }

    private Map<String, Long> buildMediaCategoryCounts() {
        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        for (MediaCategory category : MediaCategory.values()) {
            long count = mediaAssetRepository.countByCategory(category);
            categoryCounts.put(category.name(), count);
        }
        return categoryCounts;
    }
}
