package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.PasswordResetCode;
import com.andrei.demo.model.Person;
import com.andrei.demo.repository.PasswordResetCodeRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.util.PasswordUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class PasswordResetService {

    private final PersonRepository personRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordUtil passwordUtil;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public void sendResetCode(String email) {
        Person person = personRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("No account found with this email"));

        String code = generateCode();

        PasswordResetCode resetCode = new PasswordResetCode();
        resetCode.setEmail(person.getEmail());
        resetCode.setCode(code);
        resetCode.setCreatedAt(LocalDateTime.now());
        resetCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        resetCode.setUsed(false);

        passwordResetCodeRepository.save(resetCode);

        emailService.sendPasswordResetCode(person.getEmail(), code);
    }

    public void resetPassword(String email, String code, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match");
        }

        PasswordResetCode resetCode = passwordResetCodeRepository
                .findTopByEmailAndCodeAndUsedFalseOrderByCreatedAtDesc(email, code)
                .orElseThrow(() -> new ValidationException("Invalid reset code"));

        if (resetCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Reset code has expired");
        }

        Person person = personRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("No account found with this email"));

        person.setPassword(passwordUtil.hashPassword(newPassword));
        personRepository.save(person);

        resetCode.setUsed(true);
        passwordResetCodeRepository.save(resetCode);

        emailService.sendPasswordChangedConfirmation(email);
    }

    private String generateCode() {
        int number = secureRandom.nextInt(1_000_000);
        return String.format("%06d", number);
    }
}