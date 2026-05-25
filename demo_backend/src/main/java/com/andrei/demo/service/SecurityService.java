package com.andrei.demo.service;

import com.andrei.demo.model.LoginResponse;
import com.andrei.demo.model.Person;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.util.JwtUtil;
import com.andrei.demo.util.PasswordUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class SecurityService {

    private final PersonRepository personRepository;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;

    public LoginResponse login(String email, String password) {
        if (email == null || email.isBlank()) {
            return new LoginResponse(false, null, null, null, "Email is required");
        }

        if (password == null || password.isBlank()) {
            return new LoginResponse(false, null, null, null, "Password is required");
        }

        Optional<Person> maybePerson = personRepository.findByEmail(email);

        if (maybePerson.isEmpty()) {
            return new LoginResponse(
                    false,
                    null,
                    null,
                    null,
                    "Person with email " + email + " not found"
            );
        }

        Person person = maybePerson.get();

        if (passwordUtil.checkPassword(password, person.getPassword())) {
            String token = jwtUtil.createToken(person);

            return new LoginResponse(
                    true,
                    person.getRole().name(),
                    token,
                    person.getId(),
                    null
            );
        }

        return new LoginResponse(false, null, null, null, "Wrong email or password");
    }
}