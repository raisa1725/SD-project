package com.andrei.demo.service;

import com.andrei.demo.model.LoginResponse;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.util.JwtUtil;
import com.andrei.demo.util.PasswordUtil;
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

    @Mock
    private PasswordUtil passwordUtil;

    @Mock
    private JwtUtil jwtUtil;

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
        String email = "john.doe@example.com";
        String password = "Password_john123!@#";

        Person person = new Person();
        person.setName("John Doe");
        person.setEmail(email);
        person.setPassword("hashed-password");
        person.setAge(30);
        person.setRole(PersonRole.USER);

        when(personRepository.findByEmail(email)).thenReturn(Optional.of(person));
        when(passwordUtil.checkPassword(password, person.getPassword())).thenReturn(true);

        LoginResponse result = securityService.login(email, password);

        assertTrue(result.success());
        assertEquals("USER", result.role());
        assertNull(result.errorMessage());

        verify(personRepository, times(1)).findByEmail(email);
        verify(passwordUtil, times(1)).checkPassword(password, person.getPassword());
    }

    @Test
    void testLoginIncorrectPassword() {
        String email = "john@example.com";
        String password = "password";

        Person person = new Person();
        person.setEmail(email);
        person.setPassword("stored-hash");
        person.setRole(PersonRole.USER);

        when(personRepository.findByEmail(email)).thenReturn(Optional.of(person));
        when(passwordUtil.checkPassword(password, person.getPassword())).thenReturn(false);

        LoginResponse result = securityService.login(email, password);

        assertFalse(result.success());
        assertNull(result.role());
        assertEquals("Incorrect password", result.errorMessage());

        verify(personRepository, times(1)).findByEmail(email);
        verify(passwordUtil, times(1)).checkPassword(password, person.getPassword());
        verify(jwtUtil, never()).createToken(any(Person.class));
    }

    @Test
    void testLoginEmailNotFound() {
        String email = "john@example.com";
        String password = "password";

        when(personRepository.findByEmail(email)).thenReturn(Optional.empty());

        LoginResponse result = securityService.login(email, password);

        assertFalse(result.success());
        assertNull(result.role());
        assertEquals("Person with email " + email + " not found", result.errorMessage());

        verify(personRepository, times(1)).findByEmail(email);
        verifyNoInteractions(passwordUtil);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void testLoginSuccessCreatesToken() {
        String email = "john@example.com";
        String rawPassword = "Password_john123!";
        String hashedPassword = "hashed-password";
        String token = "token-123";

        Person person = new Person();
        person.setName("John Doe");
        person.setEmail(email);
        person.setPassword(hashedPassword);
        person.setAge(30);
        person.setRole(PersonRole.USER);

        when(personRepository.findByEmail(email)).thenReturn(Optional.of(person));
        when(passwordUtil.checkPassword(rawPassword, hashedPassword)).thenReturn(true);
        when(jwtUtil.createToken(person)).thenReturn(token);

        LoginResponse result = securityService.login(email, rawPassword);

        assertTrue(result.success());
        assertEquals("USER", result.role());
        assertNull(result.errorMessage());

        verify(personRepository).findByEmail(email);
        verify(passwordUtil).checkPassword(rawPassword, hashedPassword);
        verify(jwtUtil).createToken(person);
    }

    @Test
    void testLoginWrongPasswordDoesNotCreateToken() {
        String email = "john@example.com";
        String rawPassword = "Wrong_password123!";
        String hashedPassword = "hashed-password";

        Person person = new Person();
        person.setEmail(email);
        person.setPassword(hashedPassword);
        person.setRole(PersonRole.USER);

        when(personRepository.findByEmail(email)).thenReturn(Optional.of(person));
        when(passwordUtil.checkPassword(rawPassword, hashedPassword)).thenReturn(false);

        LoginResponse result = securityService.login(email, rawPassword);

        assertFalse(result.success());
        assertNull(result.role());
        assertEquals("Incorrect password", result.errorMessage());

        verify(personRepository).findByEmail(email);
        verify(passwordUtil).checkPassword(rawPassword, hashedPassword);
        verify(jwtUtil, never()).createToken(any(Person.class));
    }

    @Test
    void testLoginEmailNotFoundDoesNotCheckPasswordOrCreateToken() {
        String email = "missing@example.com";
        String password = "Password_missing123!";

        when(personRepository.findByEmail(email)).thenReturn(Optional.empty());

        LoginResponse result = securityService.login(email, password);

        assertFalse(result.success());
        assertNull(result.role());
        assertEquals("Person with email " + email + " not found", result.errorMessage());

        verify(personRepository).findByEmail(email);
        verifyNoInteractions(passwordUtil);
        verifyNoInteractions(jwtUtil);
    }
}