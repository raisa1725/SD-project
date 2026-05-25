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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class LoginControllerIntegrationTests {

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
    void testCreatePersonStoresHashedPassword() throws Exception {
        String email = "hash.test@example.com";
        String rawPassword = "Password_test123!";

        String requestBody = """
                {
                  "name": "Hash Test",
                  "password": "Password_test123!",
                  "age": 22,
                  "email": "hash.test@example.com",
                  "role": "USER"
                }
                """;

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        Person savedPerson = personRepository.findByEmail(email).orElseThrow();

        assertNotNull(savedPerson.getPassword());
        assertNotEquals(rawPassword, savedPerson.getPassword());
    }

    @Test
    void testLoginSuccessWithHashedPassword() throws Exception {
        String createPersonBody = """
                {
                  "name": "Login Success",
                  "password": "Password_login123!",
                  "age": 25,
                  "email": "login.success@example.com",
                  "role": "USER"
                }
                """;

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPersonBody))
                .andExpect(status().isOk());

        String loginBody = """
                {
                  "email": "login.success@example.com",
                  "password": "Password_login123!"
                }
                """;

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testLoginFailsWithWrongPassword() throws Exception {
        String createPersonBody = """
                {
                  "name": "Wrong Password",
                  "password": "Password_correct123!",
                  "age": 25,
                  "email": "wrong.password@example.com",
                  "role": "USER"
                }
                """;

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPersonBody))
                .andExpect(status().isOk());

        String loginBody = """
                {
                  "email": "wrong.password@example.com",
                  "password": "Wrong_password123!"
                }
                """;

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("Incorrect password"));
    }

    @Test
    void testLoginFailsWhenEmailDoesNotExist() throws Exception {
        String loginBody = """
                {
                  "email": "missing@example.com",
                  "password": "Password_missing123!"
                }
                """;

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Person with email missing@example.com not found"));
    }
}