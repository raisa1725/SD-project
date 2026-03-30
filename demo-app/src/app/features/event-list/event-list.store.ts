import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { CreateEventDto, Event } from '../../models/event.model';
import { EventService } from '../../services/event.service';
import { NotificationService } from '../../services/notification.service';

@Injectable({ providedIn: 'root' })
export class EventListStore {
  private readonly eventService = inject(EventService);
  private readonly notify = inject(NotificationService);
  private readonly pendingRequests = signal(0);

  readonly events = signal<Event[]>([]);
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
      message = 'Event not found.';
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
    this.eventService
      .getAll()
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (data) => this.events.set(data),
        error: (err: HttpErrorResponse) => this.handleError(err),
      });
  }

  create(dto: CreateEventDto): void {
    this.hasError.set(false);
    this.beginRequest();
    this.eventService
      .create(dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (created) => {
          this.events.update((list) => [...list, created]);
          this.notify.success(`Event "${created.title}" created successfully.`);
        },
        error: (err: HttpErrorResponse) => this.handleError(err),
      });
  }

  update(id: string, dto: CreateEventDto): void {
    this.hasError.set(false);
    this.beginRequest();
    this.eventService
      .update(id, dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) => {
          this.events.update((list) => list.map((e) => (e.id === updated.id ? updated : e)));
          this.notify.success(`Event "${updated.title}" updated successfully.`);
        },
        error: (err: HttpErrorResponse) => this.handleError(err),
      });
  }

  remove(id: string): void {
    const existing = this.events().find((e) => e.id === id);
    this.hasError.set(false);
    this.beginRequest();
    this.eventService
      .delete(id)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: () => {
          this.events.update((list) => list.filter((e) => e.id !== id));
          this.notify.success(`Event "${existing?.title ?? ''}" deleted successfully.`);
        },
        error: (err: HttpErrorResponse) => this.handleError(err),
      });
  }
}
