package com.andrei.demo.repository;

import com.andrei.demo.model.Event;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.Reservation;
import com.andrei.demo.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    Optional<Reservation> findByPersonAndEvent(Person person, Event event);

    Optional<Reservation> findByPersonIdAndEventId(UUID personId, UUID eventId);

    // JPA Derived Query
    List<Reservation> findByPerson(Person person);

    List<Reservation> findByEvent(Event event);

    List<Reservation> findByEventId(UUID eventId);

    List<Reservation> findByStatus(ReservationStatus status);

    // find reservations for a given person email
    @Query("SELECT r FROM Reservation r WHERE r.person.email = ?1")
    List<Reservation> findByPersonEmail(String email);

    // find reservations for a given event title
    @Query("SELECT r FROM Reservation r WHERE r.event.title = ?1")
    List<Reservation> findByEventTitle(String title);
}