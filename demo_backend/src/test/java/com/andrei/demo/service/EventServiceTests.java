package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Event;
import com.andrei.demo.model.EventCreateDTO;
import com.andrei.demo.model.EventUpdateDTO;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.repository.EventRepository;
import com.andrei.demo.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTests {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void testGetEvents() {
        Event event1 = createEvent();
        Event event2 = createEvent();
        event2.setTitle("Second Event");

        when(eventRepository.findAll()).thenReturn(List.of(event1, event2));

        List<Event> result = eventService.getEvents();

        assertEquals(2, result.size());
        assertEquals("Test Event", result.get(0).getTitle());
        assertEquals("Second Event", result.get(1).getTitle());

        verify(eventRepository).findAll();
    }

    @Test
    void testGetEventById_ExistingId() {
        UUID eventId = UUID.randomUUID();
        Event event = createEvent();
        event.setId(eventId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        Event result = eventService.getEventById(eventId);

        assertEquals(eventId, result.getId());
        assertEquals("Test Event", result.getTitle());

        verify(eventRepository).findById(eventId);
    }

    @Test
    void testGetEventById_MissingId() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> eventService.getEventById(eventId));

        verify(eventRepository).findById(eventId);
    }

    @Test
    void testAddEvent_ValidPayload() {
        Person organizer = createOrganizer();
        UUID organizerId = UUID.randomUUID();
        organizer.setId(organizerId);

        EventCreateDTO dto = createEventCreateDTO();
        dto.setOrganizerId(organizerId);

        when(personRepository.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.addEvent(dto);

        assertEquals("Test Event", result.getTitle());
        assertEquals("Test description", result.getDescription());
        assertEquals("Cluj-Napoca", result.getLocation());
        assertEquals(100, result.getMaxParticipants());
        assertEquals(organizer, result.getOrganizer());

        verify(personRepository).findById(organizerId);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testAddEvent_MissingOrganizer() {
        UUID organizerId = UUID.randomUUID();

        EventCreateDTO dto = createEventCreateDTO();
        dto.setOrganizerId(organizerId);

        when(personRepository.findById(organizerId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> eventService.addEvent(dto));

        verify(personRepository).findById(organizerId);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void testUpdateEvent_ExistingId() {
        UUID eventId = UUID.randomUUID();

        Event existingEvent = createEvent();
        existingEvent.setId(eventId);

        Person newOrganizer = createOrganizer();
        UUID newOrganizerId = UUID.randomUUID();
        newOrganizer.setId(newOrganizerId);
        newOrganizer.setEmail("new.organizer@example.com");

        Event updatedEvent = createEvent();
        updatedEvent.setTitle("Updated Event");
        updatedEvent.setDescription("Updated description");
        updatedEvent.setLocation("Bucharest");
        updatedEvent.setDate(LocalDateTime.now().plusDays(10));
        updatedEvent.setMaxParticipants(200);
        updatedEvent.setOrganizer(newOrganizer);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(personRepository.findById(newOrganizerId)).thenReturn(Optional.of(newOrganizer));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.updateEvent(eventId, updatedEvent);

        assertEquals("Updated Event", result.getTitle());
        assertEquals("Updated description", result.getDescription());
        assertEquals("Bucharest", result.getLocation());
        assertEquals(200, result.getMaxParticipants());
        assertEquals(newOrganizer, result.getOrganizer());

        verify(eventRepository).findById(eventId);
        verify(personRepository).findById(newOrganizerId);
        verify(eventRepository).save(existingEvent);
    }

    @Test
    void testUpdateEvent_MissingId() {
        UUID eventId = UUID.randomUUID();
        Event updatedEvent = createEvent();

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> eventService.updateEvent(eventId, updatedEvent));

        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void testPatchEvent_ValidPayload() {
        UUID eventId = UUID.randomUUID();

        Event existingEvent = createEvent();
        existingEvent.setId(eventId);

        EventUpdateDTO dto = new EventUpdateDTO();
        dto.setTitle("Patched Event");
        dto.setLocation("Timisoara");
        dto.setMaxParticipants(150);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.patchEvent(eventId, dto);

        assertEquals("Patched Event", result.getTitle());
        assertEquals("Timisoara", result.getLocation());
        assertEquals(150, result.getMaxParticipants());

        assertEquals("Test description", result.getDescription());
        assertEquals(existingEvent.getOrganizer(), result.getOrganizer());

        verify(eventRepository).findById(eventId);
        verify(eventRepository).save(existingEvent);
    }

    @Test
    void testPatchEvent_WithOrganizerId() {
        UUID eventId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();

        Event existingEvent = createEvent();
        existingEvent.setId(eventId);

        Person newOrganizer = createOrganizer();
        newOrganizer.setId(organizerId);
        newOrganizer.setEmail("new.organizer@example.com");

        EventUpdateDTO dto = new EventUpdateDTO();
        dto.setOrganizerId(organizerId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(personRepository.findById(organizerId)).thenReturn(Optional.of(newOrganizer));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.patchEvent(eventId, dto);

        assertEquals(newOrganizer, result.getOrganizer());

        verify(eventRepository).findById(eventId);
        verify(personRepository).findById(organizerId);
        verify(eventRepository).save(existingEvent);
    }

    @Test
    void testPatchEvent_MissingEventId() {
        UUID eventId = UUID.randomUUID();

        EventUpdateDTO dto = new EventUpdateDTO();
        dto.setTitle("Patched Event");

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> eventService.patchEvent(eventId, dto));

        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void testDeleteEvent() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.existsById(eventId)).thenReturn(true);
        doNothing().when(eventRepository).deleteById(eventId);

        eventService.deleteEvent(eventId);

        verify(eventRepository).existsById(eventId);
        verify(eventRepository).deleteById(eventId);
    }

    @Test
    void testDeleteEvent_MissingId() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.existsById(eventId)).thenReturn(false);

        assertThrows(ValidationException.class, () -> eventService.deleteEvent(eventId));

        verify(eventRepository).existsById(eventId);
        verify(eventRepository, never()).deleteById(any(UUID.class));
    }

    private EventCreateDTO createEventCreateDTO() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle("Test Event");
        dto.setDescription("Test description");
        dto.setLocation("Cluj-Napoca");
        dto.setDate(LocalDateTime.now().plusDays(5));
        dto.setMaxParticipants(100);
        return dto;
    }

    private Event createEvent() {
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setTitle("Test Event");
        event.setDescription("Test description");
        event.setLocation("Cluj-Napoca");
        event.setDate(LocalDateTime.now().plusDays(5));
        event.setMaxParticipants(100);
        event.setOrganizer(createOrganizer());
        return event;
    }

    private Person createOrganizer() {
        Person organizer = new Person();
        organizer.setId(UUID.randomUUID());
        organizer.setName("Test Organizer");
        organizer.setPassword("Organizer_pass123!@#");
        organizer.setAge(30);
        organizer.setEmail("organizer@example.com");
        organizer.setRole(PersonRole.ORGANIZER);
        return organizer;
    }
}