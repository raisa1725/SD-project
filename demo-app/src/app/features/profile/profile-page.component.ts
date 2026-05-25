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
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';

import {
  PersonFormDialogComponent,
  PersonFormDialogData,
  PersonFormDialogResult,
} from '../../components/person-form-dialog/person-form-dialog.component';

import {
  ReservationFormDialogComponent,
  ReservationFormDialogData,
  ReservationFormDialogResult,
} from '../../components/reservation-form-dialog/reservation-form-dialog.component';

import { Person, UpdatePersonDto } from '../../models/person.model';
import { Reservation, UpdateReservationDto } from '../../models/reservation.model';
import { LoginStore } from '../login/login.store';
import { PersonService } from '../../services/person.service';
import { ReservationService } from '../../services/reservation.service';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatTableModule,
    DatePipe,
  ],
  templateUrl: './profile-page.component.html',
  styleUrl: './profile-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfilePageComponent implements OnInit {
  private readonly loginStore = inject(LoginStore);
  private readonly personService = inject(PersonService);
  private readonly reservationService = inject(ReservationService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly person = signal<Person | null>(null);
  protected readonly reservations = signal<Reservation[]>([]);
  protected readonly isUser = computed(() => this.loginStore.role() === 'USER');

  protected readonly displayedColumns = [
    'event',
    'date',
    'spotsReserved',
    'status',
    'actions',
  ];

  protected readonly userReservations = computed(() => {
    const currentPerson = this.person();

    if (!currentPerson) {
      return [];
    }

    return this.reservations().filter(
      (reservation) => reservation.person.id === currentPerson.id,
    );
  });

  ngOnInit(): void {
    const email = this.loginStore.email();

    if (!email) {
      return;
    }

    this.personService
      .getByEmail(email)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((person) => {
        this.person.set(person);

        if (this.isUser()) {
          this.loadReservations();
        }
      });
  }

  protected openEditProfileDialog(): void {
    const currentPerson = this.person();

    if (!currentPerson) {
      return;
    }

    this.dialog
      .open<PersonFormDialogComponent, PersonFormDialogData, PersonFormDialogResult>(
        PersonFormDialogComponent,
        {
          data: {
            title: 'Edit Profile',
            submitLabel: 'Save',
            showPasswordField: false,
            initialValue: {
              name: currentPerson.name,
              age: currentPerson.age,
              email: currentPerson.email,
              role: currentPerson.role,
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

        const dto: UpdatePersonDto = {
          name: result.name,
          age: result.age,
          email: result.email,
          role: currentPerson.role,
        };

        this.personService
          .patch(currentPerson.id, dto)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe((updatedPerson) => {
            this.person.set(updatedPerson);
          });
      });
  }

  protected openEditReservationDialog(reservation: Reservation): void {
    this.dialog
      .open<
        ReservationFormDialogComponent,
        ReservationFormDialogData,
        ReservationFormDialogResult
      >(ReservationFormDialogComponent, {
        data: {
          title: 'Edit Reservation',
          submitLabel: 'Save',
          persons: [reservation.person],
          events: [reservation.event],
          isEdit: true,
          canEditStatus: false,
          simpleUserEdit: true,
          initialValue: {
            personId: reservation.person.id,
            eventId: reservation.event.id,
            spotsReserved: reservation.spotsReserved,
            status: reservation.status,
          },
        },
      })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) {
          return;
        }

        const dto = result as UpdateReservationDto;

        this.reservationService
          .patch(reservation.id, dto)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              alert('Reservation updated successfully!');
              this.loadReservations();
            },
            error: (error) => {
              console.error(error);
              alert('Could not update reservation.');
            },
          });
      });
  }

  private loadReservations(): void {
    this.reservationService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((reservations) => {
        this.reservations.set(reservations);
      });
  }

  protected deleteReservation(reservation: Reservation): void {
    const confirmed = confirm(
      `Are you sure you want to delete the reservation for "${reservation.event.title}"?`
    );

    if (!confirmed) {
      return;
    }

    this.reservationService
      .delete(reservation.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          alert('Reservation deleted successfully!');
          this.loadReservations();
        },
        error: (error) => {
          console.error(error);
          alert('Could not delete reservation.');
        },
      });
  }

  protected requestOrganizerRole(): void {
    const currentPerson = this.person();

    if (!currentPerson) {
      return;
    }

    const dto: UpdatePersonDto = {
      name: currentPerson.name,
      age: currentPerson.age,
      email: currentPerson.email,
      role: currentPerson.role,
      requestedRole: 'ORGANIZER',
    };

    this.personService
      .patch(currentPerson.id, dto)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((updatedPerson) => {
        this.person.set(updatedPerson);
      });
  }
}
