package com.andrei.demo.controller;

import com.andrei.demo.model.Event;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.model.Reservation;
import com.andrei.demo.model.ReservationStatus;
import com.andrei.demo.repository.EventRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class ReservationControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Person user;
    private Person organizer;
    private Event event;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        personRepository.deleteAll();

        reservationRepository.flush();
        eventRepository.flush();
        personRepository.flush();

        user = createSavedUser();
        organizer = createSavedOrganizer();
        event = createSavedEvent();
    }

    @Test
    void testGetReservations() throws Exception {
        createSavedReservation();

        mockMvc.perform(get("/reservation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].spotsReserved").value(2))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void testAddReservation_ValidPayload() throws Exception {
        String validReservationJson = """
                {
                  "personId": "%s",
                  "eventId": "%s",
                  "spotsReserved": 2
                }
                """.formatted(user.getId(), event.getId());

        mockMvc.perform(post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validReservationJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.spotsReserved").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.person.email").value("user@example.com"))
                .andExpect(jsonPath("$.event.title").value("Test Event"));
    }

    @Test
    void testAddReservation_InvalidSpotsReserved() throws Exception {
        String invalidReservationJson = """
                {
                  "personId": "%s",
                  "eventId": "%s",
                  "spotsReserved": 0
                }
                """.formatted(user.getId(), event.getId());

        mockMvc.perform(post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidReservationJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.spotsReserved")
                        .value("Reserved spots must be at least 1"));
    }

    @Test
    void testGetReservationById() throws Exception {
        Reservation reservation = createSavedReservation();

        mockMvc.perform(get("/reservation/" + reservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservation.getId().toString()))
                .andExpect(jsonPath("$.spotsReserved").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testPatchReservation_ValidPayload() throws Exception {
        Reservation reservation = createSavedReservation();

        String patchJson = """
                {
                  "spotsReserved": 3,
                  "status": "ACCEPTED"
                }
                """;

        mockMvc.perform(patch("/reservation/" + reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spotsReserved").value(3))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void testDeleteReservation() throws Exception {
        Reservation reservation = createSavedReservation();

        mockMvc.perform(delete("/reservation/" + reservation.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reservation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private Person createSavedUser() {
        Person person = new Person();
        person.setName("Test User");
        person.setPassword("User_pass123!@#");
        person.setAge(21);
        person.setEmail("user@example.com");
        person.setRole(PersonRole.USER);
        return personRepository.save(person);
    }

    private Person createSavedOrganizer() {
        Person person = new Person();
        person.setName("Test Organizer");
        person.setPassword("Organizer_pass123!@#");
        person.setAge(30);
        person.setEmail("organizer@example.com");
        person.setRole(PersonRole.ORGANIZER);
        return personRepository.save(person);
    }

    private Event createSavedEvent() {
        Event event = new Event();
        event.setTitle("Test Event");
        event.setDescription("Test description");
        event.setLocation("Cluj-Napoca");
        event.setDate(LocalDateTime.now().plusDays(5));
        event.setMaxParticipants(100);
        event.setOrganizer(organizer);
        return eventRepository.save(event);
    }

    private Reservation createSavedReservation() {
        Reservation reservation = new Reservation();
        reservation.setPerson(user);
        reservation.setEvent(event);
        reservation.setSpotsReserved(2);
        reservation.setStatus(ReservationStatus.PENDING);
        return reservationRepository.save(reservation);
    }
}