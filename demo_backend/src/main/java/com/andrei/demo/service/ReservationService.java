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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PersonRepository personRepository;
    private final EventRepository eventRepository;
    private final ReservationNotificationService reservationNotificationService;

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

        if (person.getRole() != PersonRole.USER && person.getRole() != PersonRole.ORGANIZER) {
            throw new ValidationException("Only users & organizers can reserve spots for events");
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

    @Transactional
    public Reservation updateReservation(UUID uuid, Reservation reservation) {
        Reservation existingReservation = reservationRepository.findById(uuid)
                .orElseThrow(() -> new ValidationException("Reservation with id " + uuid + " not found"));

        ReservationStatus oldStatus = existingReservation.getStatus();

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

        Reservation savedReservation = reservationRepository.save(existingReservation);
        notifyUserIfStatusChanged(oldStatus, savedReservation);

        return savedReservation;
    }

    @Transactional
    public Reservation patchReservation(UUID uuid, ReservationUpdateDTO dto) {
        Reservation reservation = reservationRepository.findById(uuid)
                .orElseThrow(() -> new ValidationException("Reservation not found"));

        ReservationStatus oldStatus = reservation.getStatus();

        if (dto.getSpotsReserved() != null) {
            reservation.setSpotsReserved(dto.getSpotsReserved());
        }

        if (dto.getStatus() != null) {
            reservation.setStatus(dto.getStatus());
        }

        Reservation savedReservation = reservationRepository.save(reservation);
        notifyUserIfStatusChanged(oldStatus, savedReservation);

        return savedReservation;
    }

    public void deleteReservation(UUID uuid) {
        if (!reservationRepository.existsById(uuid)) {
            throw new ValidationException("Reservation with id " + uuid + " not found");
        }

        reservationRepository.deleteById(uuid);
    }

    public Reservation getReservationById(UUID uuid) {
        return reservationRepository.findById(uuid)
                .orElseThrow(() -> new ValidationException("Reservation with id " + uuid + " not found"));
    }

    public List<Reservation> getOrganizerReservations(UUID organizerId) {
        return reservationRepository.findByEventOrganizerIdAndStatus(
                organizerId,
                ReservationStatus.PENDING
        );
    }

    public List<Reservation> getOrganizerEventReservations(UUID organizerId) {
        return reservationRepository.findByEventOrganizerId(organizerId);
    }

    @Transactional
    public Reservation acceptReservation(UUID reservationId) {
        Reservation reservation = getReservationById(reservationId);
        ReservationStatus oldStatus = reservation.getStatus();

        reservation.setStatus(ReservationStatus.ACCEPTED);

        Reservation savedReservation = reservationRepository.save(reservation);
        notifyUserIfStatusChanged(oldStatus, savedReservation);

        return savedReservation;
    }

    @Transactional
    public Reservation declineReservation(UUID reservationId) {
        Reservation reservation = getReservationById(reservationId);
        ReservationStatus oldStatus = reservation.getStatus();

        reservation.setStatus(ReservationStatus.DECLINED);

        Reservation savedReservation = reservationRepository.save(reservation);
        notifyUserIfStatusChanged(oldStatus, savedReservation);

        return savedReservation;
    }

    private void notifyUserIfStatusChanged(ReservationStatus oldStatus, Reservation reservation) {
        if (oldStatus == reservation.getStatus()) {
            return;
        }

        if (reservation.getStatus() == ReservationStatus.ACCEPTED ||
                reservation.getStatus() == ReservationStatus.DECLINED) {
            reservationNotificationService.notifyUserReservationUpdated(reservation);
        }
    }


}