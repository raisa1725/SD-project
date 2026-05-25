package com.andrei.demo.controller;

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
class EventControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Person organizer;

    @BeforeEach
    void cleanUp() {
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        personRepository.deleteAll();

        organizer = new Person();
        organizer.setName("Organizer User");
        organizer.setEmail("organizer@example.com");
        organizer.setPassword("hashed-password");
        organizer.setAge(30);
        organizer.setRole(PersonRole.ORGANIZER);

        organizer = personRepository.save(organizer);
    }

    @Test
    void testAddEventSuccessfully() throws Exception {
        String date = LocalDateTime.now().plusDays(10).withNano(0).toString();

        String requestBody = """
                {
                  "title": "Mountain Hiking",
                  "description": "A hiking event in the mountains",
                  "location": "Cluj",
                  "date": "%s",
                  "maxParticipants": 20,
                  "organizerId": "%s"
                }
                """.formatted(date, organizer.getId());

        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Mountain Hiking"))
                .andExpect(jsonPath("$.location").value("Cluj"))
                .andExpect(jsonPath("$.maxParticipants").value(20));
    }

    @Test
    void testGetEventsReturnsCreatedEvent() throws Exception {
        String date = LocalDateTime.now().plusDays(12).withNano(0).toString();

        String requestBody = """
                {
                  "title": "City Walk",
                  "description": "A city walking event",
                  "location": "Bucharest",
                  "date": "%s",
                  "maxParticipants": 15,
                  "organizerId": "%s"
                }
                """.formatted(date, organizer.getId());

        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/event"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("City Walk"))
                .andExpect(jsonPath("$[0].location").value("Bucharest"));
    }

    @Test
    void testCreateEventWithPastDateReturnsBadRequest() throws Exception {
        String date = LocalDateTime.now().minusDays(1).withNano(0).toString();

        String requestBody = """
                {
                  "title": "Past Event",
                  "description": "This event should not be valid",
                  "location": "Cluj",
                  "date": "%s",
                  "maxParticipants": 10,
                  "organizerId": "%s"
                }
                """.formatted(date, organizer.getId());

        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.date").exists());
    }

    @Test
    void testCreateEventWithInvalidCapacityReturnsBadRequest() throws Exception {
        String date = LocalDateTime.now().plusDays(5).withNano(0).toString();

        String requestBody = """
                {
                  "title": "Invalid Capacity",
                  "description": "Capacity is invalid",
                  "location": "Cluj",
                  "date": "%s",
                  "maxParticipants": 0,
                  "organizerId": "%s"
                }
                """.formatted(date, organizer.getId());

        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.maxParticipants").exists());
    }

    @Test
    void testPatchEventSuccessfully() throws Exception {
        String date = LocalDateTime.now().plusDays(8).withNano(0).toString();

        String createBody = """
                {
                  "title": "Old Event Title",
                  "description": "Old description",
                  "location": "Cluj",
                  "date": "%s",
                  "maxParticipants": 10,
                  "organizerId": "%s"
                }
                """.formatted(date, organizer.getId());

        String response = mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String eventId = response.split("\"id\":\"")[1].split("\"")[0];

        String patchBody = """
                {
                  "title": "Updated Event Title",
                  "location": "Timisoara",
                  "maxParticipants": 25
                }
                """;

        mockMvc.perform(patch("/event/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Event Title"))
                .andExpect(jsonPath("$.location").value("Timisoara"))
                .andExpect(jsonPath("$.maxParticipants").value(25));
    }

    @Test
    void testDeleteEventSuccessfully() throws Exception {
        String date = LocalDateTime.now().plusDays(9).withNano(0).toString();

        String createBody = """
                {
                  "title": "Delete Event",
                  "description": "Event to delete",
                  "location": "Cluj",
                  "date": "%s",
                  "maxParticipants": 10,
                  "organizerId": "%s"
                }
                """.formatted(date, organizer.getId());

        String response = mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String eventId = response.split("\"id\":\"")[1].split("\"")[0];

        mockMvc.perform(delete("/event/" + eventId))
                .andExpect(status().isOk());
    }
}