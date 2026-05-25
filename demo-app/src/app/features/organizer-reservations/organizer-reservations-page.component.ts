import { Component, OnInit, inject, signal } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { forkJoin } from 'rxjs';
import { LoginStore } from '../login/login.store';
import { PersonService } from '../../services/person.service';
import { ReservationService } from '../../services/reservation.service';
import { Reservation } from '../../models/reservation.model';

interface EventReservationGroup {
  eventId: string;
  eventTitle: string;
  reservations: Reservation[];
}

@Component({
  selector: 'app-organizer-reservations-page',
  standalone: true,
  imports: [MatExpansionModule],
  templateUrl: './organizer-reservations-page.component.html',
  styleUrl: './organizer-reservations-page.component.scss',
})
export class OrganizerReservationsPageComponent implements OnInit {
  private readonly loginStore = inject(LoginStore);
  private readonly personService = inject(PersonService);
  private readonly reservationService = inject(ReservationService);

  protected readonly pendingReservations = signal<Reservation[]>([]);
  protected readonly groupedReservations = signal<EventReservationGroup[]>([]);

  private organizerId: string | null = null;

  ngOnInit(): void {
    const email = this.loginStore.email();

    if (!email) {
      return;
    }

    this.personService.getByEmail(email).subscribe((person) => {
      this.organizerId = person.id;
      this.refreshReservations(person.id);
    });
  }

  protected acceptReservation(reservationId: string): void {
    if (!this.organizerId) {
      return;
    }

    this.reservationService.accept(reservationId).subscribe(() => {
      this.refreshReservations(this.organizerId!);
    });
  }

  protected declineReservation(reservationId: string): void {
    if (!this.organizerId) {
      return;
    }

    this.reservationService.decline(reservationId).subscribe(() => {
      this.refreshReservations(this.organizerId!);
    });
  }

  private refreshReservations(organizerId: string): void {
    forkJoin({
      pending: this.reservationService.getPendingForOrganizer(organizerId),
      all: this.reservationService.getAllForOrganizer(organizerId),
    }).subscribe(({ pending, all }) => {
      this.pendingReservations.set(pending);
      this.groupedReservations.set(this.groupReservationsByEvent(all));
    });
  }

  private groupReservationsByEvent(reservations: Reservation[]): EventReservationGroup[] {
    const reservationsByEvent = new Map<string, EventReservationGroup>();

    reservations.forEach((reservation) => {
      const eventId = reservation.event.id;

      if (!reservationsByEvent.has(eventId)) {
        reservationsByEvent.set(eventId, {
          eventId,
          eventTitle: reservation.event.title,
          reservations: [],
        });
      }

      reservationsByEvent.get(eventId)?.reservations.push(reservation);
    });

    return Array.from(reservationsByEvent.values());
  }
}
