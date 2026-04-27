package com.andrei.demo.service;

import com.andrei.demo.model.LoginResponse;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.repository.PersonRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityServiceTests {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private SecurityService securityService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }


    @Test
    void testLoginSuccess() {
        Person person = new Person();
        person.setName("John Doe");
        person.setEmail("john.doe@example.com");
        person.setPassword("Password_john123!@#");
        person.setAge(30);
        person.setRole(PersonRole.USER);

        when(personRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(person));

        LoginResponse response = securityService.login(
                "john.doe@example.com",
                "Password_john123!@#"
        );

        assertTrue(response.success());
        assertEquals("USER", response.role());
        assertNull(response.errorMessage());

        verify(personRepository).findByEmail("john.doe@example.com");
    }

    @Test
    void testLoginIncorrectPassword() {
        String email = "john@example.com";
        String password = "password";
        Person person = new Person();
        person.setEmail(email);
        person.setPassword("wrongpassword");

        when(personRepository.findByEmail(email)).thenReturn(Optional.of(person));
        LoginResponse result = securityService.login(email, password);

        assertFalse(result.success());
        assertEquals("Incorrect password", result.errorMessage());
        verify(personRepository, times(1)).findByEmail(email);
    }

    @Test
    void testLoginEmailNotFound() {
        String email = "john@example.com";
        String password = "password";

        when(personRepository.findByEmail(email)).thenReturn(Optional.empty());
        LoginResponse result = securityService.login(email, password);

        assertFalse(result.success());
        assertEquals("Person with email " + email + " not found", result.errorMessage());
        verify(personRepository, times(1)).findByEmail(email);
    }
}