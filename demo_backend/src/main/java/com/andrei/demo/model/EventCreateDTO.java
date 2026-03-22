package com.andrei.demo.model;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EventCreateDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title should be between 3 and 100 characters")
    private String title;

    @Size(max = 500, message = "Description should be at most 500 characters")
    private String description;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 100, message = "Location should be between 2 and 100 characters")
    private String location;

    @NotNull(message = "Date is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime date;

    @NotNull(message = "Maximum number of participants is required")
    @Min(value = 1, message = "Maximum number of participants must be at least 1")
    private Integer maxParticipants;

    @NotNull(message = "Organizer id is required")
    private UUID organizerId;
}