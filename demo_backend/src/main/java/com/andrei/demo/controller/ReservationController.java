package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Reservation;
import com.andrei.demo.model.ReservationCreateDTO;
import com.andrei.demo.service.ReservationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@CrossOrigin
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping("/reservation")
    public List<Reservation> getReservations() {
        return reservationService.getReservations();
    }

    @GetMapping("/reservation/{uuid}")
    public Reservation getReservationById(@PathVariable UUID uuid) {
        return reservationService.getReservationById(uuid);
    }

    @PostMapping("/reservation")
    public Reservation addReservation(
            @Valid @RequestBody ReservationCreateDTO reservationDTO
    ) {
        return reservationService.addReservation(reservationDTO);
    }

    @PutMapping("/reservation/{uuid}")
    public Reservation updateReservation(@PathVariable UUID uuid,
                                         @RequestBody Reservation reservation)
            throws ValidationException {
        return reservationService.updateReservation(uuid, reservation);
    }

    @DeleteMapping("/reservation/{uuid}")
    public void deleteReservation(@PathVariable UUID uuid) {
        reservationService.deleteReservation(uuid);
    }
}