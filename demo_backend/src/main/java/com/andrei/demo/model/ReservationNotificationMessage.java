package com.andrei.demo.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationNotificationMessage(
        UUID reservationId,
        UUID eventId,
        String eventTitle,
        UUID userId,
        String userEmail,
        ReservationStatus status,
        String message,
        LocalDateTime createdAt
) {
}