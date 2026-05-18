package com.andrei.demo.model;

public record LoginResponse(
        Boolean success,
        String role,
        String token,
        String errorMessage
) {
}