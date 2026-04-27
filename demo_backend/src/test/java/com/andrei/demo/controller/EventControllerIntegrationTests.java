package com.andrei.demo.controller;

import com.andrei.demo.model.Event;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.repository.EventRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.ReservationRepository;
import org.hamcrest.Matchers;
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
public class EventControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Person organizer;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        personRepository.deleteAll();

        reservationRepository.flush();
        eventRepository.flush();
        personRepository.flush();

        organizer = new Person();
        organizer.setName("Test Organizer");
        organizer.setPassword("Organizer_pass123!@#");
        organizer.setAge(30);
        organizer.setEmail("organizer@example.com");
        organizer.setRole(PersonRole.ORGANIZER);

        organizer = personRepository.save(organizer);
    }

    @Test
    void testGetEvents() throws Exception {
        createSavedEvent();

        mockMvc.perform(get("/event"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[*].title",
                        Matchers.contains("Test Event")))
                .andExpect(jsonPath("$[*].location",
                        Matchers.contains("Cluj-Napoca")));
    }

    @Test
    void testAddEvent_ValidPayload() throws Exception {
        String validEventJson = """
                {
                  "title": "Spring Workshop",
                  "description": "Backend testing workshop",
                  "location": "Cluj-Napoca",
                  "date": "%s",
                  "maxParticipants": 50,
                  "organizerId": "%s"
                }
                """.formatted(LocalDateTime.now().plusDays(5), organizer.getId());

        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEventJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Spring Workshop"))
                .andExpect(jsonPath("$.description").value("Backend testing workshop"))
                .andExpect(jsonPath("$.location").value("Cluj-Napoca"))
                .andExpect(jsonPath("$.maxParticipants").value(50));
    }

    @Test
    void testAddEvent_InvalidPastDate() throws Exception {
        String invalidEventJson = """
                {
                  "title": "Old Event",
                  "description": "This event has a past date",
                  "location": "Cluj-Napoca",
                  "date": "%s",
                  "maxParticipants": 50,
                  "organizerId": "%s"
                }
                """.formatted(LocalDateTime.now().minusDays(1), organizer.getId());

        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEventJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.date").value("Event date must be in the future"));
    }

    @Test
    void testAddEvent_InvalidCapacity() throws Exception {
        String invalidEventJson = """
                {
                  "title": "Invalid Capacity Event",
                  "description": "This event has invalid capacity",
                  "location": "Cluj-Napoca",
                  "date": "%s",
                  "maxParticipants": 0,
                  "organizerId": "%s"
                }
                """.formatted(LocalDateTime.now().plusDays(5), organizer.getId());

        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEventJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.maxParticipants")
                        .value("Maximum number of participants must be at least 1"));
    }

    @Test
    void testGetEventById() throws Exception {
        Event event = createSavedEvent();

        mockMvc.perform(get("/event/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(event.getId().toString()))
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.location").value("Cluj-Napoca"))
                .andExpect(jsonPath("$.maxParticipants").value(100));
    }

    @Test
    void testPatchEvent_ValidPayload() throws Exception {
        Event event = createSavedEvent();

        String patchJson = """
                {
                  "title": "Updated Event",
                  "location": "Bucharest",
                  "maxParticipants": 120
                }
                """;

        mockMvc.perform(patch("/event/" + event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Event"))
                .andExpect(jsonPath("$.location").value("Bucharest"))
                .andExpect(jsonPath("$.maxParticipants").value(120));
    }

    @Test
    void testDeleteEvent() throws Exception {
        Event event = createSavedEvent();

        mockMvc.perform(delete("/event/" + event.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/event"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
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
}