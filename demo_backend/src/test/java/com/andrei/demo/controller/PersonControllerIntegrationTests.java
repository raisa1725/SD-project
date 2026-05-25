package com.andrei.demo.controller;

import com.andrei.demo.model.Person;
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

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
class PersonControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void cleanUp() {
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        personRepository.deleteAll();
    }

    @Test
    void testAddPersonSuccessfully() throws Exception {
        String requestBody = """
                {
                  "name": "John Doe",
                  "password": "Password_john123!",
                  "age": 30,
                  "email": "john@example.com",
                  "role": "USER"
                }
                """;

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testCreatePersonStoresHashedPassword() throws Exception {
        String rawPassword = "Password_test123!";

        String requestBody = """
                {
                  "name": "Hash Test",
                  "password": "Password_test123!",
                  "age": 22,
                  "email": "hash.person@example.com",
                  "role": "USER"
                }
                """;

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        Person savedPerson = personRepository.findByEmail("hash.person@example.com").orElseThrow();

        assertNotNull(savedPerson.getPassword());
        assertNotEquals(rawPassword, savedPerson.getPassword());
    }

    @Test
    void testCreatePersonWithInvalidEmailReturnsBadRequest() throws Exception {
        String requestBody = """
                {
                  "name": "Invalid Email",
                  "password": "Password_valid123!",
                  "age": 22,
                  "email": "not-an-email",
                  "role": "USER"
                }
                """;

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void testCreatePersonWithWeakPasswordReturnsBadRequest() throws Exception {
        String requestBody = """
                {
                  "name": "Weak Password",
                  "password": "abc",
                  "age": 22,
                  "email": "weak.password@example.com",
                  "role": "USER"
                }
                """;

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void testGetPeopleReturnsCreatedPerson() throws Exception {
        String requestBody = """
                {
                  "name": "List User",
                  "password": "Password_list123!",
                  "age": 24,
                  "email": "list.user@example.com",
                  "role": "USER"
                }
                """;

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/person"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("list.user@example.com"));
    }

    @Test
    void testPatchPersonSuccessfully() throws Exception {
        String createBody = """
                {
                  "name": "Patch User",
                  "password": "Password_patch123!",
                  "age": 24,
                  "email": "patch.user@example.com",
                  "role": "USER"
                }
                """;

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk());

        Person savedPerson = personRepository.findByEmail("patch.user@example.com").orElseThrow();

        String patchBody = """
                {
                  "name": "Updated Patch User",
                  "age": 25
                }
                """;

        mockMvc.perform(patch("/person/" + savedPerson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Patch User"))
                .andExpect(jsonPath("$.age").value(25));
    }

    @Test
    void testDeletePersonSuccessfully() throws Exception {
        String createBody = """
                {
                  "name": "Delete User",
                  "password": "Password_delete123!",
                  "age": 24,
                  "email": "delete.user@example.com",
                  "role": "USER"
                }
                """;

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk());

        Person savedPerson = personRepository.findByEmail("delete.user@example.com").orElseThrow();

        mockMvc.perform(delete("/person/" + savedPerson.getId()))
                .andExpect(status().isOk());
    }
}