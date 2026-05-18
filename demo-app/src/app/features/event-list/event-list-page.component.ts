import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { ConfirmDeleteDialogComponent } from '../../components/confirm-delete-dialog/confirm-delete-dialog.component';
import {
  EventFormDialogComponent,
  EventFormDialogData,
  EventFormDialogResult,
} from '../../components/event-form-dialog/event-form-dialog.component';
import { Event } from '../../models/event.model';
import { Person } from '../../models/person.model';
import { PersonService } from '../../services/person.service';
import { EventListStore } from './event-list.store';
import { Router } from '@angular/router';
import { LoginStore } from '../login/login.store';
import { ReservationService } from '../../services/reservation.service';
import { EventDetailsDialogComponent } from '../../components/event-details-dialog/event-details-dialog';

@Component({
  selector: 'app-event-list-page',
  standalone: true,
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    DatePipe,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule
  ],
  templateUrl: './event-list-page.component.html',
  styleUrl: './event-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventListPageComponent implements OnInit {
  private readonly dialog = inject(MatDialog);
  private readonly store = inject(EventListStore);
  private readonly personService = inject(PersonService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reservationService = inject(ReservationService);

  protected readonly events = this.store.events;
  protected readonly hasError = this.store.hasError;
  protected readonly isLoading = this.store.isLoading;
  protected readonly organizers = signal<Person[]>([]);
  protected readonly searchTitle = signal('');
  protected readonly searchLocation = signal('');
  protected readonly upcomingOnly = signal(false);
  private readonly loginStore = inject(LoginStore);
  private readonly router = inject(Router);
  protected readonly role = this.loginStore.role;

  protected get displayedColumns(): string[] {
    if (this.role() === 'USER') {
      return [
        'title',
        'location',
        'date',
        'maxParticipants',
        'organizer',
        'reservation',
      ];
    }

    return [
      'title',
      'location',
      'date',
      'maxParticipants',
      'organizer',
      'actions',
    ];
  }

  ngOnInit(): void {
    this.store.load();

    this.personService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((persons) => {
        this.organizers.set(
          persons.filter((p) => p.role === 'ORGANIZER' || p.role === 'ADMIN')
        );
      });
  }

  protected logout(): void {
    this.loginStore.logout();
    void this.router.navigate(['/login']);
  }

  protected applyFilters(): void {
    this.store.search({
      title: this.searchTitle(),
      location: this.searchLocation(),
      upcoming: this.upcomingOnly(),
    });
  }

  protected resetFilters(): void {
    this.searchTitle.set('');
    this.searchLocation.set('');
    this.upcomingOnly.set(false);
    this.store.load();
  }

  protected openCreateDialog(): void {
    if (this.isLoading()) return;

    if (this.role() === 'ORGANIZER') {
      void this.router.navigate(['/organizer/my-events']);
      return;
    }

    this.dialog
      .open<EventFormDialogComponent, EventFormDialogData, EventFormDialogResult>(
        EventFormDialogComponent,
        {
          data: {
            title: 'Create Event',
            submitLabel: 'Create',
            organizers: this.organizers(),
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;
        this.store.create(result);
      });
  }

  protected makeReservation(event: Event): void {
    const email = this.loginStore.email();

    if (!email) {
      return;
    }

    this.personService
      .getByEmail(email)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((person) => {
        this.reservationService
          .create({
            personId: person.id,
            eventId: event.id,
            spotsReserved: 1,
          })
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe();
      });
  }

  protected openEditDialog(event: Event): void {
    if (this.isLoading()) return;

    this.dialog
      .open<EventFormDialogComponent, EventFormDialogData, EventFormDialogResult>(
        EventFormDialogComponent,
        {
          data: {
            title: 'Edit Event',
            submitLabel: 'Save',
            organizers: this.organizers(),
            initialValue: {
              title: event.title,
              description: event.description,
              location: event.location,
              date: event.date,
              maxParticipants: event.maxParticipants,
              organizerId: event.organizer.id,
            },
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;
        this.store.update(event.id, result);
      });
  }

  protected openDeleteDialog(event: Event): void {
    if (this.isLoading()) return;

    this.dialog
      .open<ConfirmDeleteDialogComponent, { person: { name: string } }, boolean>(
        ConfirmDeleteDialogComponent,
        {
          data: {
            person: { name: event.title },
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (!confirmed) return;
        this.store.remove(event.id);
      });
  }

  protected readonly viewMode = signal<'grid' | 'list'>('grid');

  protected readonly visibleEvents = computed(() => {
    const role = this.role();
    const email = this.loginStore.email();

    if (role === 'ORGANIZER') {
      return this.events().filter((event) => event.organizer.email !== email);
    }

    return this.events();
  });

  protected openDetails(event: Event): void {
    const role = this.role();
    const email = this.loginStore.email();

    const canReserve =
      role === 'USER' ||
      (role === 'ORGANIZER' && event.organizer.email !== email);

    this.dialog
      .open<EventDetailsDialogComponent, { event: Event; canReserve: boolean }, 'reserve'>(
        EventDetailsDialogComponent,
        {
          data: { event, canReserve },
        }
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (result === 'reserve') {
          this.makeReservation(event);
        }
      });
  }
}
