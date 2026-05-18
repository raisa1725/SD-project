package com.andrei.demo.controller;

import com.andrei.demo.model.ForgotPasswordRequest;
import com.andrei.demo.model.ResetPasswordRequest;
import com.andrei.demo.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendResetCode(request.email());

        return Map.of(
                "message", "Password reset code was sent to your email"
        );
    }

    @PostMapping("/reset")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(
                request.email(),
                request.code(),
                request.newPassword(),
                request.confirmPassword()
        );

        return Map.of(
                "message", "Password was successfully reset"
        );
    }
}