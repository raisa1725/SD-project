package com.andrei.demo.service;

import com.andrei.demo.model.Reservation;
import com.andrei.demo.model.ReservationNotificationMessage;
import com.andrei.demo.model.ReservationStatus;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ReservationNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyUserReservationUpdated(Reservation reservation) {
        String userEmail = reservation.getPerson().getEmail();
        String destination = "/topic/user/" + userEmail + "/notifications";

        System.out.println("======================================");
        System.out.println("RESERVATION NOTIFICATION SENT");
        System.out.println("Reservation id: " + reservation.getId());
        System.out.println("User email: " + userEmail);
        System.out.println("Event: " + reservation.getEvent().getTitle());
        System.out.println("Status: " + reservation.getStatus());
        System.out.println("Destination: " + destination);
        System.out.println("Message: " + buildTextMessage(reservation));
        System.out.println("======================================");

        messagingTemplate.convertAndSend(
                destination,
                buildMessage(reservation)
        );
    }

    private ReservationNotificationMessage buildMessage(Reservation reservation) {
        return new ReservationNotificationMessage(
                reservation.getId(),
                reservation.getEvent().getId(),
                reservation.getEvent().getTitle(),
                reservation.getPerson().getId(),
                reservation.getPerson().getEmail(),
                reservation.getStatus(),
                buildTextMessage(reservation),
                LocalDateTime.now()
        );
    }

    private String buildTextMessage(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.ACCEPTED) {
            return "Your reservation for " + reservation.getEvent().getTitle() + " was accepted!";
        }

        if (reservation.getStatus() == ReservationStatus.DECLINED) {
            return "Your reservation for " + reservation.getEvent().getTitle() + " was declined.";
        }

        return "Your reservation for " + reservation.getEvent().getTitle() + " was updated.";
    }
}