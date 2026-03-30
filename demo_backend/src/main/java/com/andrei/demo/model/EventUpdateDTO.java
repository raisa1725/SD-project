package com.andrei.demo.model;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EventUpdateDTO {

    @Size(min = 3, max = 100, message = "Title should be between 3 and 100 characters")
    private String title;

    @Size(max = 500, message = "Description should be at most 500 characters")
    private String description;

    @Size(min = 2, max = 100, message = "Location should be between 2 and 100 characters")
    private String location;

    @Future(message = "Event date must be in the future")
    private LocalDateTime date;

    @Min(value = 1, message = "Maximum number of participants must be at least 1")
    private Integer maxParticipants;

    private UUID organizerId;
}