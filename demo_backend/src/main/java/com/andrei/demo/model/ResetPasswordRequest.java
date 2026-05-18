package com.andrei.demo.model;

import com.andrei.demo.validator.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Code is required")
        String code,

        @NotBlank(message = "New password is required")
        @StrongPassword(message = "Password must contain at least 8 characters, including uppercase, lowercase, digit, and special character")
        String newPassword,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {
}