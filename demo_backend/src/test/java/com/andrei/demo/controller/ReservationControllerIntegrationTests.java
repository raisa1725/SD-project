package com.andrei.demo.controller;

import com.andrei.demo.model.Event;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.repository.EventRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "MAIL_USERNAME=test@example.com",
        "MAIL_PASSWORD=test-password",
        "app.mail.from=test@example.com",
        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "spring.mail.username=test@example.com",
        "spring.mail.password=test-password",
        "spring.mail.properties.mail.smtp.auth=false",
        "spring.mail.properties.mail.smtp.starttls.enable=false"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ReservationControllerIntegrationTests {

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
    void cleanUp() {
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        personRepository.deleteAll();

        user = new Person();
        user.setName("Normal User");
        user.setEmail("normal.user@example.com");
        user.setPassword("hashed-password");
        user.setAge(22);
        user.setRole(PersonRole.USER);
        user = personRepository.save(user);

        organizer = new Person();
        organizer.setName("Organizer User");
        organizer.setEmail("reservation.organizer@example.com");
        organizer.setPassword("hashed-password");
        organizer.setAge(30);
        organizer.setRole(PersonRole.ORGANIZER);
        organizer = personRepository.save(organizer);

        event = new Event();
        event.setTitle("Reservation Event");
        event.setDescription("Event used for reservation tests");
        event.setLocation("Cluj");
        event.setDate(LocalDateTime.now().plusDays(10));
        event.setMaxParticipants(30);
        event.setOrganizer(organizer);
        event = eventRepository.save(event);
    }

    @Test
    void testAddReservationSuccessfully() throws Exception {
        String requestBody = """
                {
                  "personId": "%s",
                  "eventId": "%s",
                  "spotsReserved": 2
                }
                """.formatted(user.getId(), event.getId());

        mockMvc.perform(post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spotsReserved").value(2));
    }

    @Test
    void testGetReservationsReturnsCreatedReservation() throws Exception {
        String requestBody = """
                {
                  "personId": "%s",
                  "eventId": "%s",
                  "spotsReserved": 1
                }
                """.formatted(user.getId(), event.getId());

        mockMvc.perform(post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reservation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].spotsReserved").value(1));
    }

    @Test
    void testCreateReservationWithZeroSpotsReturnsBadRequest() throws Exception {
        String requestBody = """
                {
                  "personId": "%s",
                  "eventId": "%s",
                  "spotsReserved": 0
                }
                """.formatted(user.getId(), event.getId());

        mockMvc.perform(post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.spotsReserved").exists());
    }
}