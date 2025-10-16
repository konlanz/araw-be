package com.araw.araw.domain.event.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_dates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "session_date", nullable = false)
    private LocalDateTime sessionDate;

    @Column(name = "session_end_date")
    private LocalDateTime sessionEndDate;

    @Column(name = "session_name")
    private String sessionName;

    @Column(name = "session_description")
    private String sessionDescription;

    @Column(name = "instructor_name")
    private String instructorName;

    @Column(name = "location")
    private String location;

    @Column(name = "is_online")
    private Boolean isOnline = false;

    @Column(name = "meeting_link")
    private String meetingLink;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}

