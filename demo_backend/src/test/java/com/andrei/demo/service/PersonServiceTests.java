package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PersonServiceTests {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PasswordUtil passwordUtil;

    @InjectMocks
    private PersonService personService;

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
    void testGetPeople() {
        // given:
        List<Person> people = List.of(new Person(), new Person());

        // when:
        when(personRepository.findAll()).thenReturn(people);
        List<Person> result = personService.getPeople();

        // then:
        assertEquals(2, result.size());
        verify(personRepository, times(1)).findAll();
        assertEquals(people, result);
    }

    @Test
    void testAddPerson() {
        // given:
        PersonCreateDTO personDTO = new PersonCreateDTO();
        personDTO.setName("John");
        personDTO.setPassword("password");
        personDTO.setAge(30);
        personDTO.setEmail("john@example.com");
        personDTO.setRole(PersonRole.USER);

        when(personRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordUtil.hashPassword("password")).thenReturn("hashed-password");
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when:
        Person result = personService.addPerson(personDTO);

        // then:
        assertEquals("John", result.getName());
        assertEquals(30, result.getAge());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("hashed-password", result.getPassword());
        assertEquals(PersonRole.USER, result.getRole());

        verify(personRepository, times(1)).findByEmail("john@example.com");
        verify(passwordUtil, times(1)).hashPassword("password");
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    void testUpdatePerson() {
        // given:
        UUID uuid = UUID.randomUUID();

        Person existingPerson = new Person();
        existingPerson.setId(uuid);
        existingPerson.setName("John");
        existingPerson.setAge(30);
        existingPerson.setEmail("john@example.com");
        existingPerson.setPassword("old-hash");

        Person updatePayload = new Person();
        updatePayload.setId(uuid);
        updatePayload.setName("Jane");
        updatePayload.setAge(25);
        updatePayload.setEmail("jane@example.com");

        when(personRepository.findById(uuid)).thenReturn(Optional.of(existingPerson));
        when(personRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when:
        Person result = personService.updatePerson(uuid, updatePayload);

        // then:
        assertEquals("Jane", result.getName());
        assertEquals(25, result.getAge());
        assertEquals("jane@example.com", result.getEmail());

        // Password should stay unchanged in normal user update
        assertEquals("old-hash", result.getPassword());

        verify(personRepository, times(1)).findById(uuid);
        verify(personRepository, times(1)).findByEmail("jane@example.com");
        verify(personRepository, times(1)).save(any(Person.class));

        verifyNoInteractions(passwordUtil);
    }

    @Test
    void testUpdatePersonNotFound() {
        // given:
        UUID uuid = UUID.randomUUID();
        Person person = new Person();

        // when:
        when(personRepository.findById(uuid)).thenReturn(Optional.empty());

        // then:
        assertThrows(ValidationException.class, () -> personService.updatePerson(uuid, person));
        verify(personRepository, times(1)).findById(uuid);
        verify(personRepository, never()).save(any(Person.class));
        verifyNoInteractions(passwordUtil);
    }

    @Test
    void testDeletePerson() {
        UUID personId = UUID.randomUUID();

        when(personRepository.existsById(personId)).thenReturn(true);
        doNothing().when(personRepository).deleteById(personId);

        personService.deletePerson(personId);

        verify(personRepository).existsById(personId);
        verify(personRepository).deleteById(personId);
    }

    @Test
    void testDeletePerson_MissingId() {
        UUID personId = UUID.randomUUID();

        when(personRepository.existsById(personId)).thenReturn(false);

        assertThrows(ValidationException.class, () -> personService.deletePerson(personId));

        verify(personRepository).existsById(personId);
        verify(personRepository, never()).deleteById(any(UUID.class));
    }
}