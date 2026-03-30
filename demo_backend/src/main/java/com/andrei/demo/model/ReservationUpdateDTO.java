package com.andrei.demo.model;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ReservationUpdateDTO {

    @Min(value = 1, message = "Reserved spots must be at least 1")
    private Integer spotsReserved;

    private ReservationStatus status;
}