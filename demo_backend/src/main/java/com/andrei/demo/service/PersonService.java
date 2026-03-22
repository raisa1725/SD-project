package com.andrei.demo.service;

import com.andrei.demo.config.FieldValidationException;
import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonCreateDTO;
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
            throw new FieldValidationException("email", "Email must be unique. This email is already already used");
        }

        Person person = new Person();
        person.setName(personDTO.getName());
        person.setAge(personDTO.getAge());
        person.setEmail(personDTO.getEmail());
        person.setPassword(personDTO.getPassword());

        return personRepository.save(person);
    }

    public Person updatePerson(UUID uuid, Person person) {
        Optional<Person> personOptional = personRepository.findById(uuid);

        if (personOptional.isEmpty()) {
            throw new ValidationException("Person with id " + uuid + " not found");
        }

        Optional<Person> personWithSameEmail = personRepository.findByEmail(person.getEmail());
        if (personWithSameEmail.isPresent() && !personWithSameEmail.get().getId().equals(uuid)) {
            throw new FieldValidationException("email", "Email must be unique. This email is already already used");
        }

        Person existingPerson = personOptional.get();
        existingPerson.setName(person.getName());
        existingPerson.setAge(person.getAge());
        existingPerson.setEmail(person.getEmail());
        existingPerson.setPassword(person.getPassword());

        return personRepository.save(existingPerson);
    }

    public Person updatePerson2(UUID uuid, Person person) throws ValidationException{
        return personRepository
                        .findById(uuid)
                        .map(existingPerson -> {
                            existingPerson.setName(person.getName());
                            existingPerson.setAge(person.getAge());
                            existingPerson.setEmail(person.getEmail());
                            existingPerson.setPassword(person.getPassword());
                            return personRepository.save(existingPerson);
                        })
                        .orElseThrow(
                                () -> new ValidationException("Person with id " + uuid + " not found")
                        );
    }

    public void deletePerson(UUID uuid) {
        personRepository.deleteById(uuid);
    }

    public Person getPersonByEmail(String email) {
        return personRepository.findByEmail(email).orElseThrow(
                () -> new IllegalStateException("Person with email " + email + " not found"));
    }

    public Person getPersonById(UUID uuid) {
        return personRepository.findById(uuid).orElseThrow(
                () -> new IllegalStateException("Person with id " + uuid + " not found"));
    }



    //mai fa patch verifica toate datele sa existe
}
