package com.andrei.demo.service;

import com.andrei.demo.config.FieldValidationException;
import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.model.PersonUpdateDTO;
import com.andrei.demo.repository.PersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;

    public List<Person> getPeople() {
        return personRepository.findAll();
    }

    public Person addPerson(PersonCreateDTO personDTO) {
        if (personRepository.findByEmail(personDTO.getEmail()).isPresent()) {
            throw new FieldValidationException("email", "Email must be unique. This email is already used");
        }

        Person person = new Person();
        person.setName(personDTO.getName());
        person.setAge(personDTO.getAge());
        person.setEmail(personDTO.getEmail());
        person.setPassword(personDTO.getPassword());
        person.setRole(personDTO.getRole() != null ? personDTO.getRole() : PersonRole.USER);

        return personRepository.save(person);
    }

    public Person updatePerson(UUID uuid, Person person) {
        Optional<Person> personOptional = personRepository.findById(uuid);

        if (personOptional.isEmpty()) {
            throw new ValidationException("Person with id " + uuid + " not found");
        }

        Optional<Person> personWithSameEmail = personRepository.findByEmail(person.getEmail());
        if (personWithSameEmail.isPresent() && !personWithSameEmail.get().getId().equals(uuid)) {
            throw new FieldValidationException("email", "Email must be unique. This email is already used");
        }

        Person existingPerson = personOptional.get();
        existingPerson.setName(person.getName());
        existingPerson.setAge(person.getAge());
        existingPerson.setEmail(person.getEmail());
        if (person.getPassword() != null && !person.getPassword().isBlank()) {
            existingPerson.setPassword(person.getPassword());
        }
        if (person.getRole() != null) {
            existingPerson.setRole(person.getRole());
        }

        return personRepository.save(existingPerson);
    }

    public Person patchPerson(UUID uuid, PersonUpdateDTO dto) {
        Person existingPerson = personRepository.findById(uuid)
                .orElseThrow(() -> new ValidationException("Person with id " + uuid + " not found"));

        if (dto.getName() != null) {
            if (dto.getName().length() < 2 || dto.getName().length() > 100) {
                throw new FieldValidationException("name", "Name should be between 2 and 100 characters");
            }
            existingPerson.setName(dto.getName());
        }

        if (dto.getEmail() != null) {
            Optional<Person> personWithSameEmail = personRepository.findByEmail(dto.getEmail());
            if (personWithSameEmail.isPresent() && !personWithSameEmail.get().getId().equals(uuid)) {
                throw new FieldValidationException("email", "Email must be unique. This email is already used");
            }
            existingPerson.setEmail(dto.getEmail());
        }

        if (dto.getAge() != null) {
            existingPerson.setAge(dto.getAge());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existingPerson.setPassword(dto.getPassword());
        }

        if (dto.getRole() != null) {
            existingPerson.setRole(dto.getRole());
        }

        return personRepository.save(existingPerson);
    }

    public void deletePerson(UUID uuid) {
        if (!personRepository.existsById(uuid)) {
            throw new ValidationException("Person with id " + uuid + " not found");
        }
        personRepository.deleteById(uuid);
    }

    public Person getPersonByEmail(String email) {
        return personRepository.findByEmail(email).orElseThrow(
                () -> new ValidationException("Person with email " + email + " not found"));
    }

    public Person getPersonById(UUID uuid) {
        return personRepository.findById(uuid).orElseThrow(
                () -> new ValidationException("Person with id " + uuid + " not found"));
    }
}