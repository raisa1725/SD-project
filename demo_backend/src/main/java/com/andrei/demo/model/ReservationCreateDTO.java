package com.andrei.demo.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReservationCreateDTO {

    @NotNull(message = "Person id is required")
    private UUID personId;

    @NotNull(message = "Event id is required")
    private UUID eventId;

    @NotNull(message = "Number of reserved spots is required")
    @Min(value = 1, message = "Reserved spots must be at least 1")
    private Integer spotsReserved;
}