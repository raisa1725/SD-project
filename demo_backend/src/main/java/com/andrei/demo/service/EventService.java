package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Event;
import com.andrei.demo.model.EventCreateDTO;
import com.andrei.demo.model.EventUpdateDTO;
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

    public Event patchEvent(UUID uuid, EventUpdateDTO dto) {
        Event existingEvent = eventRepository.findById(uuid)
                .orElseThrow(() -> new ValidationException("Event with id " + uuid + " not found"));

        if (dto.getTitle() != null) {
            existingEvent.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            existingEvent.setDescription(dto.getDescription());
        }
        if (dto.getLocation() != null) {
            existingEvent.setLocation(dto.getLocation());
        }
        if (dto.getDate() != null) {
            existingEvent.setDate(dto.getDate());
        }
        if (dto.getMaxParticipants() != null) {
            existingEvent.setMaxParticipants(dto.getMaxParticipants());
        }
        if (dto.getOrganizerId() != null) {
            Person organizer = personRepository.findById(dto.getOrganizerId())
                    .orElseThrow(() -> new ValidationException("Organizer with id " + dto.getOrganizerId() + " not found"));
            if (organizer.getRole() != PersonRole.ORGANIZER) {
                throw new ValidationException("Only organizers can be assigned to events");
            }
            existingEvent.setOrganizer(organizer);
        }

        return eventRepository.save(existingEvent);
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