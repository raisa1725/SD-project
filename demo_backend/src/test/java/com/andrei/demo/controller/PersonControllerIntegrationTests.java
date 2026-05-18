package com.andrei.demo.controller;

import com.andrei.demo.model.Person;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.util.JwtUtil;
import com.andrei.demo.repository.EventRepository;
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
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class PersonControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private static final String FIXTURE_PATH = "src/test/resources/fixtures/";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        personRepository.deleteAll();

        reservationRepository.flush();
        eventRepository.flush();
        personRepository.flush();

        seedDatabase();
        initializeAuthToken();
    }

    private void seedDatabase() throws Exception {
        String seedDataJson = loadFixture("seed_person.json");
        List<Person> people = objectMapper.readValue(seedDataJson, new TypeReference<>() {});
        personRepository.saveAll(people);
    }

    private void initializeAuthToken() {
        Person authPerson = personRepository.findAll().stream().findFirst().orElseThrow(
                () -> new IllegalStateException("No seeded person available for auth token"));
        authToken = jwtUtil.createToken(authPerson);
    }

    @Test
    void testGetPeople() throws Exception {
        mockMvc.perform(get("/person")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[*].name",
                        Matchers.containsInAnyOrder("John Doe", "Jane Doe")))
                .andExpect(jsonPath("$[*].age",
                        Matchers.containsInAnyOrder(30, 25)))
                .andExpect(jsonPath("$[*].email",
                        Matchers.containsInAnyOrder(
                                "john.doe@example.com", "jane.doe@example.com"
                        )));
    }

    @Test
    void testAddPerson_ValidPayload() throws Exception {
        String validPersonJson = loadFixture("valid_person.json");

        mockMvc.perform(post("/person")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPersonJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.password", Matchers.startsWith("$2")))
                .andExpect(jsonPath("$.age").value(28))
                .andExpect(jsonPath("$.email").value("alice.smith@example.com"));
    }

    @Test
    void testAddPerson_InvalidPayload() throws Exception {
        String invalidPersonJson = loadFixture("invalid_person.json");

        mockMvc.perform(post("/person")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPersonJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name")
                        .value("Name should be between 2 and 100 characters"))
                .andExpect(jsonPath("$.password")
                        .value("Password must contain at least 8 characters, including uppercase, lowercase, digit, and special character"))
                .andExpect(jsonPath("$.age")
                        .value("Age is required"))
                .andExpect(jsonPath("$.email")
                        .value("Email is required"));
    }

    @Test
    void testGetPersonById() throws Exception {
        Person person = personRepository.findAll().get(0);

        mockMvc.perform(get("/person/" + person.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(person.getId().toString()))
                .andExpect(jsonPath("$.name").value(person.getName()))
                .andExpect(jsonPath("$.email").value(person.getEmail()));
    }

    @Test
    void testGetPersonByEmail() throws Exception {
        mockMvc.perform(get("/person/email/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void testPatchPerson_ValidPayload() throws Exception {
        Person person = personRepository.findAll().get(0);

        String patchJson = """
            {
              "name": "John Updated",
              "age": 35,
              "password": "UpdatedPass123!@#"
            }
            """;

        mockMvc.perform(patch("/person/" + person.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.age").value(35))
                .andExpect(jsonPath("$.email").value(person.getEmail()));
    }

    @Test
    void testDeletePerson() throws Exception {
        Person person = personRepository.findAll().get(0);

        mockMvc.perform(delete("/person/" + person.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/person"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testAddPerson_DuplicateEmail() throws Exception {
        String duplicatePersonJson = """
            {
              "name": "Duplicate User",
              "password": "Securepass123!@#",
              "age": 22,
              "email": "john.doe@example.com",
              "role": "USER"
            }
            """;

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicatePersonJson))
                .andExpect(status().isBadRequest());
    }




    private String loadFixture(String fileName) throws IOException {
        return Files.readString(Paths.get(FIXTURE_PATH + fileName));
    }
}