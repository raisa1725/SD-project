package com.andrei.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public void sendPasswordResetCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Password reset code");
        message.setText(
                "Hello,\n\n" +
                        "Your password reset code is: " + code + "\n\n" +
                        "This code is valid for 10 minutes.\n\n" +
                        "If you did not request this, you can ignore this email."
        );

        mailSender.send(message);
    }

    public void sendPasswordChangedConfirmation(String to) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Your password was changed");
        message.setText(
                "Hello,\n\n" +
                        "Your password was successfully changed.\n\n" +
                        "If this was not you, please contact support immediately."
        );

        mailSender.send(message);
    }
}