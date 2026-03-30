import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { CreateReservationDto, Reservation, UpdateReservationDto } from '../../models/reservation.model';
import { ReservationService } from '../../services/reservation.service';
import { NotificationService } from '../../services/notification.service';

@Injectable({ providedIn: 'root' })
export class ReservationListStore {
  private readonly reservationService = inject(ReservationService);
  private readonly notify = inject(NotificationService);
  private readonly pendingRequests = signal(0);

  readonly reservations = signal<Reservation[]>([]);
  readonly hasError = signal(false);
  readonly isLoading = computed(() => this.pendingRequests() > 0);

  private beginRequest(): void {
    this.pendingRequests.update((count) => count + 1);
  }

  private endRequest(): void {
    this.pendingRequests.update((count) => Math.max(0, count - 1));
  }

  private handleError(err: HttpErrorResponse): void {
    this.hasError.set(true);
    const body: Record<string, unknown> =
      typeof err.error === 'object' && err.error !== null
        ? (err.error as Record<string, unknown>)
        : {};
    const entries = Object.entries(body);
    let message: string;
    if (entries.length) {
      message = entries
        .map(([field, msg]) =>
          field === 'error' ? String(msg) : `${field}: ${String(msg)}`
        )
        .join('\n');
    } else if (err.status === 0) {
      message = 'Cannot reach the server. Is the backend running?';
    } else if (err.status === 404) {
      message = 'Reservation not found.';
    } else if (err.status === 500) {
      message = 'Server error. Please try again.';
    } else {
      message = 'An unexpected error occurred.';
    }
    this.notify.error(message);
  }

  load(): void {
    this.hasError.set(false);
    this.beginRequest();
    this.reservationService
      .getAll()
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (data) => this.reservations.set(data),
        error: (err: HttpErrorResponse) => this.handleError(err),
      });
  }

  create(dto: CreateReservationDto): void {
    this.hasError.set(false);
    this.beginRequest();
    this.reservationService
      .create(dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (created) => {
          this.reservations.update((list) => [...list, created]);
          this.notify.success('Reservation created successfully.');
        },
        error: (err: HttpErrorResponse) => this.handleError(err),
      });
  }

  update(id: string, dto: UpdateReservationDto): void {
    this.hasError.set(false);
    this.beginRequest();
    this.reservationService
      .patch(id, dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) => {
          this.reservations.update((list) =>
            list.map((r) => (r.id === updated.id ? updated : r)),
          );
          this.notify.success('Reservation updated successfully.');
        },
        error: (err: HttpErrorResponse) => this.handleError(err),
      });
  }

  remove(id: string): void {
    this.hasError.set(false);
    this.beginRequest();
    this.reservationService
      .delete(id)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: () => {
          this.reservations.update((list) => list.filter((r) => r.id !== id));
          this.notify.success('Reservation deleted successfully.');
        },
        error: (err: HttpErrorResponse) => this.handleError(err),
      });
  }
}
