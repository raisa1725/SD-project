package com.andrei.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private Person organizer;
}