package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Event;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.model.Reservation;
import com.andrei.demo.model.ReservationCreateDTO;
import com.andrei.demo.model.ReservationStatus;
import com.andrei.demo.model.ReservationUpdateDTO;
import com.andrei.demo.repository.EventRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PersonRepository personRepository;
    private final EventRepository eventRepository;

    public List<Reservation> getReservations() {
        return reservationRepository.findAll();
    }

    public Reservation addReservation(ReservationCreateDTO reservationDTO) {
        Person person = personRepository.findById(reservationDTO.getPersonId())
                .orElseThrow(() ->
                        new ValidationException("Person with id " + reservationDTO.getPersonId() + " not found"));

        Event event = eventRepository.findById(reservationDTO.getEventId())
                .orElseThrow(() ->
                        new ValidationException("Event with id " + reservationDTO.getEventId() + " not found"));

        if (person.getRole() == PersonRole.ORGANIZER) {
            throw new ValidationException("Organizers cannot reserve spots for events");
        }

        if (reservationDTO.getSpotsReserved() <= 0) {
            throw new ValidationException("Reserved spots must be greater than 0");
        }

        Optional<Reservation> existingReservation =
                reservationRepository.findByPersonIdAndEventId(person.getId(), event.getId());

        if (existingReservation.isPresent()) {
            throw new ValidationException("This user already has a reservation for this event");
        }

        int alreadyReservedSpots = reservationRepository.findByEventId(event.getId())
                .stream()
                .filter(r -> r.getStatus() != ReservationStatus.DECLINED)
                .mapToInt(Reservation::getSpotsReserved)
                .sum();

        int remainingSpots = event.getMaxParticipants() - alreadyReservedSpots;

        if (remainingSpots <= 0) {
            throw new ValidationException("No more spots available for this event");
        }

        if (reservationDTO.getSpotsReserved() > remainingSpots) {
            throw new ValidationException("Not enough available spots. Only " + remainingSpots + " spots left");
        }

        Reservation reservation = new Reservation();
        reservation.setPerson(person);
        reservation.setEvent(event);
        reservation.setSpotsReserved(reservationDTO.getSpotsReserved());
        reservation.setStatus(ReservationStatus.PENDING);

        return reservationRepository.save(reservation);
    }

    public Reservation updateReservation(UUID uuid, Reservation reservation) {
        Optional<Reservation> reservationOptional = reservationRepository.findById(uuid);

        if (reservationOptional.isEmpty()) {
            throw new ValidationException("Reservation with id " + uuid + " not found");
        }

        Reservation existingReservation = reservationOptional.get();

        if (reservation.getStatus() != null) {
            existingReservation.setStatus(reservation.getStatus());
        }

        if (reservation.getSpotsReserved() != null && reservation.getSpotsReserved() > 0) {
            int alreadyReservedSpots = reservationRepository.findByEventId(existingReservation.getEvent().getId())
                    .stream()
                    .filter(r -> !r.getId().equals(existingReservation.getId()))
                    .filter(r -> r.getStatus() != ReservationStatus.DECLINED)
                    .mapToInt(Reservation::getSpotsReserved)
                    .sum();

            int remainingSpots = existingReservation.getEvent().getMaxParticipants() - alreadyReservedSpots;

            if (reservation.getSpotsReserved() > remainingSpots) {
                throw new ValidationException("Not enough available spots for this event");
            }

            existingReservation.setSpotsReserved(reservation.getSpotsReserved());
        }

        return reservationRepository.save(existingReservation);
    }

    public Reservation patchReservation(UUID uuid, ReservationUpdateDTO dto) {
        Reservation existingReservation = reservationRepository.findById(uuid)
                .orElseThrow(() -> new ValidationException("Reservation with id " + uuid + " not found"));

        if (dto.getStatus() != null) {
            existingReservation.setStatus(dto.getStatus());
        }

        if (dto.getSpotsReserved() != null && dto.getSpotsReserved() > 0) {
            int alreadyReservedSpots = reservationRepository.findByEventId(existingReservation.getEvent().getId())
                    .stream()
                    .filter(r -> !r.getId().equals(existingReservation.getId()))
                    .filter(r -> r.getStatus() != ReservationStatus.DECLINED)
                    .mapToInt(Reservation::getSpotsReserved)
                    .sum();

            int remainingSpots = existingReservation.getEvent().getMaxParticipants() - alreadyReservedSpots;

            if (dto.getSpotsReserved() > remainingSpots) {
                throw new ValidationException("Not enough available spots. Only " + remainingSpots + " spots left");
            }

            existingReservation.setSpotsReserved(dto.getSpotsReserved());
        }

        return reservationRepository.save(existingReservation);
    }

    public void deleteReservation(UUID uuid) {
        if (!reservationRepository.existsById(uuid)) {
            throw new ValidationException("Reservation with id " + uuid + " not found");
        }
        reservationRepository.deleteById(uuid);
    }

    public Reservation getReservationById(UUID uuid) {
        return reservationRepository.findById(uuid).orElseThrow(
                () -> new ValidationException("Reservation with id " + uuid + " not found"));
    }
}