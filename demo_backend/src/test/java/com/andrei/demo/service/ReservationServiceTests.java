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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTests {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void testGetReservations() {
        Reservation reservation1 = createReservation();
        Reservation reservation2 = createReservation();
        reservation2.setSpotsReserved(4);

        when(reservationRepository.findAll()).thenReturn(List.of(reservation1, reservation2));

        List<Reservation> result = reservationService.getReservations();

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getSpotsReserved());
        assertEquals(4, result.get(1).getSpotsReserved());

        verify(reservationRepository).findAll();
    }

    @Test
    void testGetReservationById_ExistingId() {
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = createReservation();
        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        Reservation result = reservationService.getReservationById(reservationId);

        assertEquals(reservationId, result.getId());
        assertEquals(2, result.getSpotsReserved());
        assertEquals(ReservationStatus.PENDING, result.getStatus());

        verify(reservationRepository).findById(reservationId);
    }

    @Test
    void testGetReservationById_MissingId() {
        UUID reservationId = UUID.randomUUID();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class,
                () -> reservationService.getReservationById(reservationId));

        verify(reservationRepository).findById(reservationId);
    }

    @Test
    void testAddReservation_ValidPayload() {
        UUID personId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Person person = createUser();
        person.setId(personId);

        Event event = createEvent();
        event.setId(eventId);

        ReservationCreateDTO dto = createReservationCreateDTO();
        dto.setPersonId(personId);
        dto.setEventId(eventId);

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.addReservation(dto);

        assertEquals(person, result.getPerson());
        assertEquals(event, result.getEvent());
        assertEquals(2, result.getSpotsReserved());
        assertEquals(ReservationStatus.PENDING, result.getStatus());

        verify(personRepository).findById(personId);
        verify(eventRepository).findById(eventId);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void testAddReservation_MissingPerson() {
        UUID personId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        ReservationCreateDTO dto = createReservationCreateDTO();
        dto.setPersonId(personId);
        dto.setEventId(eventId);

        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class,
                () -> reservationService.addReservation(dto));

        verify(personRepository).findById(personId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testAddReservation_MissingEvent() {
        UUID personId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Person person = createUser();
        person.setId(personId);

        ReservationCreateDTO dto = createReservationCreateDTO();
        dto.setPersonId(personId);
        dto.setEventId(eventId);

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class,
                () -> reservationService.addReservation(dto));

        verify(personRepository).findById(personId);
        verify(eventRepository).findById(eventId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testUpdateReservation_ExistingId() {
        UUID reservationId = UUID.randomUUID();

        Reservation existingReservation = createReservation();
        existingReservation.setId(reservationId);

        Reservation updatedReservation = createReservation();
        updatedReservation.setSpotsReserved(5);
        updatedReservation.setStatus(ReservationStatus.ACCEPTED);

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(existingReservation));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.updateReservation(reservationId, updatedReservation);

        assertEquals(5, result.getSpotsReserved());
        assertEquals(ReservationStatus.ACCEPTED, result.getStatus());

        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository).save(existingReservation);
    }

    @Test
    void testUpdateReservation_MissingId() {
        UUID reservationId = UUID.randomUUID();

        Reservation updatedReservation = createReservation();

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

        assertThrows(ValidationException.class,
                () -> reservationService.updateReservation(reservationId, updatedReservation));

        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testPatchReservation_ValidPayload() {
        UUID reservationId = UUID.randomUUID();

        Reservation existingReservation = createReservation();
        existingReservation.setId(reservationId);

        ReservationUpdateDTO dto = new ReservationUpdateDTO();
        dto.setSpotsReserved(3);
        dto.setStatus(ReservationStatus.ACCEPTED);

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(existingReservation));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.patchReservation(reservationId, dto);

        assertEquals(3, result.getSpotsReserved());
        assertEquals(ReservationStatus.ACCEPTED, result.getStatus());

        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository).save(existingReservation);
    }

    @Test
    void testPatchReservation_MissingId() {
        UUID reservationId = UUID.randomUUID();

        ReservationUpdateDTO dto = new ReservationUpdateDTO();
        dto.setSpotsReserved(3);

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

        assertThrows(ValidationException.class,
                () -> reservationService.patchReservation(reservationId, dto));

        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testDeleteReservation() {
        UUID reservationId = UUID.randomUUID();

        when(reservationRepository.existsById(reservationId)).thenReturn(true);
        doNothing().when(reservationRepository).deleteById(reservationId);

        reservationService.deleteReservation(reservationId);

        verify(reservationRepository).existsById(reservationId);
        verify(reservationRepository).deleteById(reservationId);
    }

    @Test
    void testDeleteReservation_MissingId() {
        UUID reservationId = UUID.randomUUID();

        when(reservationRepository.existsById(reservationId)).thenReturn(false);

        assertThrows(ValidationException.class,
                () -> reservationService.deleteReservation(reservationId));

        verify(reservationRepository).existsById(reservationId);
        verify(reservationRepository, never()).deleteById(any());
    }

    private ReservationCreateDTO createReservationCreateDTO() {
        ReservationCreateDTO dto = new ReservationCreateDTO();
        dto.setSpotsReserved(2);
        return dto;
    }

    private Reservation createReservation() {
        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setPerson(createUser());
        reservation.setEvent(createEvent());
        reservation.setSpotsReserved(2);
        reservation.setStatus(ReservationStatus.PENDING);
        return reservation;
    }

    private Person createUser() {
        Person person = new Person();
        person.setId(UUID.randomUUID());
        person.setName("Test User");
        person.setPassword("User_pass123!@#");
        person.setAge(21);
        person.setEmail("user@example.com");
        person.setRole(PersonRole.USER);
        return person;
    }

    private Person createOrganizer() {
        Person person = new Person();
        person.setId(UUID.randomUUID());
        person.setName("Test Organizer");
        person.setPassword("Organizer_pass123!@#");
        person.setAge(30);
        person.setEmail("organizer@example.com");
        person.setRole(PersonRole.ORGANIZER);
        return person;
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
}