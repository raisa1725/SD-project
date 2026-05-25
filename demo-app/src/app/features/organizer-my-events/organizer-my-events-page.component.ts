import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

import {
  EventFormDialogComponent,
  EventFormDialogData,
  EventFormDialogResult,
} from '../../components/event-form-dialog/event-form-dialog.component';

import { EventDetailsDialogComponent } from '../../components/event-details-dialog/event-details-dialog';

import { Event } from '../../models/event.model';
import { Person } from '../../models/person.model';

import { LoginStore } from '../login/login.store';
import { EventListStore } from '../event-list/event-list.store';
import { PersonService } from '../../services/person.service';
import { EventService } from '../../services/event.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-organizer-my-events-page',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
  ],
  templateUrl: './organizer-my-events-page.component.html',
  styleUrl: './organizer-my-events-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrganizerMyEventsPageComponent implements OnInit {
  private readonly dialog = inject(MatDialog);
  private readonly store = inject(EventListStore);
  private readonly loginStore = inject(LoginStore);
  private readonly personService = inject(PersonService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly events = this.store.events;
  protected readonly isLoading = this.store.isLoading;
  protected readonly hasError = this.store.hasError;
  private readonly eventService = inject(EventService);

  protected readonly organizer = signal<Person | null>(null);
  protected readonly viewMode = signal<'grid' | 'list'>('grid');

  protected readonly myEvents = computed(() => {
    const currentOrganizer = this.organizer();

    if (!currentOrganizer) {
      return [];
    }

    return this.events().filter(
      (event) => event.organizer.id === currentOrganizer.id,
    );
  });

  ngOnInit(): void {
    const email = this.loginStore.email();

    if (!email) {
      void this.router.navigate(['/login']);
      return;
    }

    this.personService
      .getByEmail(email)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((person) => {
        this.organizer.set(person);
        this.store.load();
      });
  }

  protected openCreateDialog(): void {
    const currentOrganizer = this.organizer();

    if (!currentOrganizer || this.isLoading()) {
      return;
    }

    this.dialog
      .open<EventFormDialogComponent, EventFormDialogData, EventFormDialogResult>(
        EventFormDialogComponent,
        {
          data: {
            title: 'Create Event',
            submitLabel: 'Create',
            organizers: [currentOrganizer],
            showOrganizerField: false,
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) {
          return;
        }

        this.store.create({
          ...result,
          organizerId: currentOrganizer.id,
        });
      });
  }

  protected openEditDialog(event: Event): void {
    const currentOrganizer = this.organizer();

    if (!currentOrganizer || this.isLoading()) {
      return;
    }

    this.dialog
      .open<EventFormDialogComponent, EventFormDialogData, EventFormDialogResult>(
        EventFormDialogComponent,
        {
          data: {
            title: 'Edit Event',
            submitLabel: 'Save',
            organizers: [currentOrganizer],
            showOrganizerField: false,
            initialValue: {
              title: event.title,
              description: event.description,
              location: event.location,
              date: event.date,
              maxParticipants: event.maxParticipants,
              organizerId: currentOrganizer.id,
            },
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) {
          return;
        }

        this.store.update(event.id, {
          ...result,
          organizerId: currentOrganizer.id,
        });
      });
  }

  protected openDeleteDialog(event: Event): void {
    const confirmed = confirm(
      `Are you sure you want to delete "${event.title}"?`
    );

    if (!confirmed) {
      return;
    }

    this.eventService
      .delete(event.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          alert('Event deleted successfully!');
          this.store.load();
        },
        error: (error: unknown): void => {
          console.error(error);

          let message = 'Something went wrong.';

          if (error instanceof HttpErrorResponse) {
            const errorBody: unknown = error.error;

            if (typeof errorBody === 'string') {
              message = errorBody;
            } else if (
              typeof errorBody === 'object' &&
              errorBody !== null &&
              'message' in errorBody &&
              typeof errorBody.message === 'string'
            ) {
              message = errorBody.message;
            } else if (
              typeof errorBody === 'object' &&
              errorBody !== null &&
              'error' in errorBody &&
              typeof errorBody.error === 'string'
            ) {
              message = errorBody.error;
            } else {
              message = error.message;
            }
          }

          alert(message);
        }
      });
  }

  protected openDetails(event: Event): void {
    this.dialog.open(EventDetailsDialogComponent, {
      data: {
        event,
        canReserve: false,
      },
    });
  }
}
