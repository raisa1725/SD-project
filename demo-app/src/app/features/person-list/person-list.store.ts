import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { CreatePersonDto, Person, UpdatePersonDto } from '../../models/person.model';
import { PersonService } from '../../services/person.service';
import { NotificationService } from '../../services/notification.service';

@Injectable({ providedIn: 'root' })
export class PersonListStore {
  private readonly personService = inject(PersonService);
  private readonly notify = inject(NotificationService);
  private readonly pendingRequests = signal(0);

  readonly persons = signal<Person[]>([]);
  readonly hasError = signal(false);
  readonly isLoading = computed(() => this.pendingRequests() > 0);

  private beginRequest(): void {
    this.pendingRequests.update((count) => count + 1);
  }

  private endRequest(): void {
    this.pendingRequests.update((count) => Math.max(0, count - 1));
  }

  // eslint-disable-next-line @typescript-eslint/no-redundant-type-constituents
  private handleError(err: HttpErrorResponse | Error | unknown): void {
    this.hasError.set(true);
    this.notify.error(this.extractErrorMessage(err));
  }

  // eslint-disable-next-line @typescript-eslint/no-redundant-type-constituents
  private extractErrorMessage(err: HttpErrorResponse | Error | unknown): string {
    if (!(err instanceof HttpErrorResponse)) {
      return 'An unexpected error occurred.';
    }
    const body: Record<string, unknown> =
      typeof err.error === 'object' && err.error !== null
        ? (err.error as Record<string, unknown>)
        : {};
    const entries = Object.entries(body);
    if (entries.length) {
      return entries
        .map(([field, msg]) =>
          field === 'error' ? String(msg) : `${this.humanizeField(field)}: ${String(msg)}`
        )
        .join('\n');
    }
    if (err.status === 0) return 'Cannot reach the server. Is the backend running?';
    if (err.status === 404) return 'Person not found.';
    if (err.status === 500) return 'Server error. Please try again.';
    return 'An unexpected error occurred.';
  }

  private humanizeField(field: string): string {
    return field.charAt(0).toUpperCase() + field.slice(1).replace(/([A-Z])/g, ' $1');
  }

  load(): void {
    this.hasError.set(false);
    this.beginRequest();
    this.personService
      .getAll()
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (data) => this.persons.set(data),
        error: (err: HttpErrorResponse) => this.handleError(err),
      });
  }

  create(dto: CreatePersonDto): void {
    this.hasError.set(false);
    this.beginRequest();
    this.personService
      .create(dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (created) => {
          this.persons.update((list) => [...list, created]);
          this.notify.success(`Person "${created.name}" created successfully.`);
        },
        error: (err: HttpErrorResponse) => this.handleError(err),
      });
  }

  update(id: string, dto: UpdatePersonDto): void {
    const existing = this.persons().find((p) => p.id === id);
    if (!existing) return;

    const payload: CreatePersonDto = {
      name: dto.name ?? existing.name,
      age: dto.age ?? existing.age,
      email: dto.email ?? existing.email,
      password: dto.password ?? existing.password,
      role: dto.role ?? existing.role,
    };

    this.hasError.set(false);
    this.beginRequest();
    this.personService
      .update(id, payload)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) => {
          this.persons.update((list) =>
            list.map((person) => (person.id === updated.id ? updated : person)),
          );
          this.notify.success(`Person "${updated.name}" updated successfully.`);
        },
        error: (err: HttpErrorResponse) => this.handleError(err),
      });
  }

  remove(id: string): void {
    const existing = this.persons().find((p) => p.id === id);
    this.hasError.set(false);
    this.beginRequest();
    this.personService
      .delete(id)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: () => {
          this.persons.update((list) => list.filter((person) => person.id !== id));
          this.notify.success(`Person "${existing?.name ?? ''}" deleted successfully.`);
        },
        error: (err: HttpErrorResponse) => this.handleError(err),
      });
  }
}
