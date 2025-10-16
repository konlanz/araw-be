package com.araw.araw.domain.participant.service;


import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.participant.enitity.Participant;
import com.araw.araw.domain.participant.enitity.ParticipantProgress;
import com.araw.araw.domain.participant.repository.ParticipantRepository;
import com.araw.araw.domain.participant.valueobject.Achievement;
import com.araw.araw.domain.participant.valueobject.AchievementLevel;
import com.araw.araw.domain.participant.valueobject.AchievementType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantDomainService {

    private final ParticipantRepository participantRepository;


    public Achievement awardAchievement(Participant participant, Event event,
                                        AchievementType type, String title,
                                        String description, Integer score) {
        Achievement achievement = Achievement.builder()
                .participant(participant)
                .event(event)
                .achievementType(type)
                .title(title)
                .description(description)
                .score(score)
                .earnedDate(LocalDateTime.now())
                .build();

        if (score != null && achievement.getMaxScore() != null) {
            double percentage = achievement.getPercentage();
            achievement.setAchievementLevel(AchievementLevel.fromScore(percentage));
        }

        participant.addAchievement(achievement);
        participantRepository.save(participant);

        return achievement;
    }


    public void recordProgress(Participant participant, Event event,
                               String progressType, String title, String description) {
        ParticipantProgress progress = ParticipantProgress.builder()
                .participant(participant)
                .event(event)
                .progressType(progressType)
                .title(title)
                .description(description)
                .achievementDate(LocalDateTime.now())
                .build();

        participant.updateProgress(progress);
        participantRepository.save(participant);
    }


    public double calculateEngagementScore(Participant participant) {
        double score = 0.0;

        int eventsAttended = participant.getTotalEventsAttended() != null
                ? participant.getTotalEventsAttended()
                : 0;
        double eventScore = Math.min(eventsAttended * 10, 40);

        int achievementCount = participant.getAchievements() != null
                ? participant.getAchievements().size()
                : 0;
        double achievementScore = Math.min(achievementCount * 5, 30);

        int hoursParticipated = participant.getTotalHoursParticipated() != null
                ? participant.getTotalHoursParticipated()
                : 0;
        double hoursScore = Math.min(hoursParticipated * 0.5, 20);

        long featuredAchievements = participant.getAchievements() != null
                ? participant.getAchievements().stream()
                .filter(achievement -> Boolean.TRUE.equals(achievement.getIsFeatured()))
                .count()
                : 0;
        double featuredScore = Math.min(featuredAchievements * 5, 10);

        score = eventScore + achievementScore + hoursScore + featuredScore;
        return Math.min(score, 100.0);
    }


    public void evaluateForAlumniStatus(Participant participant) {
        int eventsAttended = participant.getTotalEventsAttended() != null
                ? participant.getTotalEventsAttended()
                : 0;
        int hoursParticipated = participant.getTotalHoursParticipated() != null
                ? participant.getTotalHoursParticipated()
                : 0;
        int achievementsCount = participant.getAchievements() != null
                ? participant.getAchievements().size()
                : 0;

        boolean shouldBeAlumni = eventsAttended >= 5 ||
                hoursParticipated >= 100 ||
                achievementsCount >= 10;

        boolean hasCompletedMajorProgram = participant.getAchievements() != null &&
                participant.getAchievements().stream()
                        .anyMatch(a -> a.getAchievementType() == AchievementType.CERTIFICATION ||
                                a.getAchievementType() == AchievementType.COMPLETION);

        if (hasCompletedMajorProgram) {
            shouldBeAlumni = true;
        }

        if (shouldBeAlumni && !Boolean.TRUE.equals(participant.getIsAlumni())) {
            participant.promoteToAlumni();
            participantRepository.save(participant);
        }
    }


    public Participant mergeParticipants(UUID primaryId, UUID duplicateId) {
        Participant primary = participantRepository.findById(primaryId)
                .orElseThrow(() -> new IllegalArgumentException("Primary participant not found"));

        Participant duplicate = participantRepository.findById(duplicateId)
                .orElseThrow(() -> new IllegalArgumentException("Duplicate participant not found"));

        if (duplicate.getAttendedEvents() != null) {
            duplicate.getAttendedEvents().forEach(primary::attendEvent);
        }

        if (duplicate.getAchievements() != null) {
            duplicate.getAchievements().forEach(achievement -> {
                achievement.setParticipant(primary);
                primary.getAchievements().add(achievement);
            });
        }

        if (duplicate.getProgressRecords() != null) {
            duplicate.getProgressRecords().forEach(progress -> {
                progress.setParticipant(primary);
                primary.getProgressRecords().add(progress);
            });
        }

        int primaryEvents = primary.getTotalEventsAttended() != null ? primary.getTotalEventsAttended() : 0;
        int duplicateEvents = duplicate.getTotalEventsAttended() != null ? duplicate.getTotalEventsAttended() : 0;
        primary.setTotalEventsAttended(primaryEvents + duplicateEvents);

        int primaryHours = primary.getTotalHoursParticipated() != null ? primary.getTotalHoursParticipated() : 0;
        int duplicateHours = duplicate.getTotalHoursParticipated() != null ? duplicate.getTotalHoursParticipated() : 0;
        primary.setTotalHoursParticipated(primaryHours + duplicateHours);

        participantRepository.save(primary);
        participantRepository.delete(duplicate);

        return primary;
    }


    public List<String> getParticipantMilestones(Participant participant) {
        List<String> milestones = new java.util.ArrayList<>();

        int eventsAttended = participant.getTotalEventsAttended() != null
                ? participant.getTotalEventsAttended()
                : 0;
        if (eventsAttended >= 1) milestones.add("First Event Attended");
        if (eventsAttended >= 5) milestones.add("5 Events Milestone");
        if (eventsAttended >= 10) milestones.add("10 Events Milestone");
        if (eventsAttended >= 25) milestones.add("25 Events Milestone");

        int achievements = participant.getAchievements() != null
                ? participant.getAchievements().size()
                : 0;
        if (achievements >= 1) milestones.add("First Achievement");
        if (achievements >= 5) milestones.add("5 Achievements");
        if (achievements >= 10) milestones.add("10 Achievements");

        int hours = participant.getTotalHoursParticipated() != null
                ? participant.getTotalHoursParticipated()
                : 0;
        if (hours >= 10) milestones.add("10 Hours of Learning");
        if (hours >= 50) milestones.add("50 Hours of Learning");
        if (hours >= 100) milestones.add("100 Hours of Learning");
        if (hours >= 500) milestones.add("500 Hours of Learning");

        if (Boolean.TRUE.equals(participant.getIsAlumni())) milestones.add("Alumni Status");
        if (Boolean.TRUE.equals(participant.getIsFeaturedAlumni())) milestones.add("Featured Alumni");

        return milestones;
    }
}
