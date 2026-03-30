import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatToolbar } from '@angular/material/toolbar';
import { ConfirmDeleteDialogComponent } from '../../components/confirm-delete-dialog/confirm-delete-dialog.component';
import {
  ReservationFormDialogComponent,
  ReservationFormDialogData,
  ReservationFormDialogResult,
} from '../../components/reservation-form-dialog/reservation-form-dialog.component';
import { Reservation, CreateReservationDto, UpdateReservationDto } from '../../models/reservation.model';
import { Person } from '../../models/person.model';
import { Event } from '../../models/event.model';
import { ReservationListStore } from './reservation-list.store';
import { PersonService } from '../../services/person.service';
import { EventService } from '../../services/event.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-reservation-list-page',
  standalone: true,
  imports: [MatTableModule, MatButtonModule, MatIconModule, MatDialogModule, MatToolbar],
  templateUrl: './reservation-list-page.component.html',
  styleUrl: './reservation-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReservationListPageComponent implements OnInit {
  private readonly dialog = inject(MatDialog);
  private readonly store = inject(ReservationListStore);
  private readonly personService = inject(PersonService);
  private readonly eventService = inject(EventService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly reservations = this.store.reservations;
  protected readonly hasError = this.store.hasError;
  protected readonly isLoading = this.store.isLoading;
  protected readonly persons = signal<Person[]>([]);
  protected readonly events = signal<Event[]>([]);
  protected readonly displayedColumns = ['person', 'event', 'spotsReserved', 'status', 'actions'];

  ngOnInit(): void {
    this.store.load();
    forkJoin({
      persons: this.personService.getAll(),
      events: this.eventService.getAll(),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(({ persons, events }) => {
        this.persons.set(persons.filter((p) => p.role === 'USER'));
        this.events.set(events);
      });
  }

  protected openCreateDialog(): void {
    if (this.isLoading()) return;

    this.dialog
      .open<ReservationFormDialogComponent, ReservationFormDialogData, ReservationFormDialogResult>(
        ReservationFormDialogComponent,
        {
          data: {
            title: 'Create Reservation',
            submitLabel: 'Create',
            persons: this.persons(),
            events: this.events(),
            isEdit: false,
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;
        this.store.create(result as CreateReservationDto);
      });
  }

  protected openEditDialog(reservation: Reservation): void {
    if (this.isLoading()) return;

    this.dialog
      .open<ReservationFormDialogComponent, ReservationFormDialogData, ReservationFormDialogResult>(
        ReservationFormDialogComponent,
        {
          data: {
            title: 'Edit Reservation',
            submitLabel: 'Save',
            persons: this.persons(),
            events: this.events(),
            isEdit: true,
            initialValue: {
              personId: reservation.person.id,
              eventId: reservation.event.id,
              spotsReserved: reservation.spotsReserved,
              status: reservation.status,
            },
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;
        this.store.update(reservation.id, result as UpdateReservationDto);
      });
  }

  protected openDeleteDialog(reservation: Reservation): void {
    if (this.isLoading()) return;

    this.dialog
      .open<ConfirmDeleteDialogComponent, { person: { name: string } }, boolean>(
        ConfirmDeleteDialogComponent,
        { data: { person: { name: `${reservation.person.name} → ${reservation.event.title}` } } },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (!confirmed) return;
        this.store.remove(reservation.id);
      });
  }
}
