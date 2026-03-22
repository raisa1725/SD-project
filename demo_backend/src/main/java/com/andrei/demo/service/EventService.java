package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Event;
import com.andrei.demo.model.EventCreateDTO;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.repository.EventRepository;
import com.andrei.demo.repository.PersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final PersonRepository personRepository;

    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    public Event addEvent(EventCreateDTO eventDTO) throws ValidationException {
        Person organizer = personRepository.findById(eventDTO.getOrganizerId())
                .orElseThrow(() ->
                        new ValidationException("Organizer with id " + eventDTO.getOrganizerId() + " not found"));

        if (organizer.getRole() != PersonRole.ORGANIZER) {
            throw new ValidationException("Only organizers can create events");
        }

        Event event = new Event();
        event.setTitle(eventDTO.getTitle());
        event.setDescription(eventDTO.getDescription());
        event.setLocation(eventDTO.getLocation());
        event.setDate(eventDTO.getDate());
        event.setMaxParticipants(eventDTO.getMaxParticipants());
        event.setOrganizer(organizer);

        return eventRepository.save(event);
    }

    public Event updateEvent(UUID uuid, Event event) {
        Optional<Event> eventOptional = eventRepository.findById(uuid);

        if (eventOptional.isEmpty()) {
            throw new ValidationException("Event with id " + uuid + " not found");
        }

        Event existingEvent = eventOptional.get();

        if (event.getOrganizer() != null) {
            Person organizer = personRepository.findById(event.getOrganizer().getId())
                    .orElseThrow(() ->
                            new ValidationException("Organizer with id " + event.getOrganizer().getId() + " not found"));

            if (organizer.getRole() != PersonRole.ORGANIZER) {
                throw new ValidationException("Only organizers can be assigned to events");
            }

            existingEvent.setOrganizer(organizer);
        }

        existingEvent.setTitle(event.getTitle());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setLocation(event.getLocation());
        existingEvent.setDate(event.getDate());
        existingEvent.setMaxParticipants(event.getMaxParticipants());

        return eventRepository.save(existingEvent);
    }

    public Event updateEvent2(UUID uuid, Event event) {
        return eventRepository.findById(uuid)
                .map(existingEvent -> {
                    if (event.getOrganizer() != null) {
                        Person organizer = null;
                        try {
                            organizer = personRepository.findById(event.getOrganizer().getId())
                                    .orElseThrow(() ->
                                            new ValidationException("Organizer with id " + event.getOrganizer().getId() + " not found"));
                        } catch (ValidationException e) {
                            throw new RuntimeException(e);
                        }

                        if (organizer.getRole() != PersonRole.ORGANIZER) {
                            try {
                                throw new ValidationException("Only organizers can be assigned to events");
                            } catch (ValidationException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        existingEvent.setOrganizer(organizer);
                    }

                    existingEvent.setTitle(event.getTitle());
                    existingEvent.setDescription(event.getDescription());
                    existingEvent.setLocation(event.getLocation());
                    existingEvent.setDate(event.getDate());
                    existingEvent.setMaxParticipants(event.getMaxParticipants());

                    return eventRepository.save(existingEvent);
                })
                .orElseThrow(() ->
                        new ValidationException("Event with id " + uuid + " not found"));
    }

    public void deleteEvent(UUID uuid) throws ValidationException {
        if (!eventRepository.existsById(uuid)) {
            throw new ValidationException("Event with id " + uuid + " not found");
        }

        eventRepository.deleteById(uuid);
    }

    public Event getEventById(UUID uuid) throws ValidationException {
        return eventRepository.findById(uuid).orElseThrow(
                () -> new ValidationException("Event with id " + uuid + " not found"));
    }
}