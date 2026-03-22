package com.andrei.demo.repository;

import com.andrei.demo.model.Event;
import com.andrei.demo.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    Optional<Event> findByTitle(String title);

    // JPA Derived Query
    List<Event> findByLocation(String location);

    List<Event> findByOrganizer(Person organizer);

    List<Event> findByDateAfter(LocalDateTime date);

    // find events whose title starts or ends with the given string
    @Query("SELECT e FROM Event e WHERE e.title LIKE CONCAT(?1, '%') OR e.title LIKE CONCAT('%', ?1)")
    List<Event> findByTitleApproximate(String title);

    List<Event> findByTitleStartingWithOrTitleEndingWith(String start, String end);
}