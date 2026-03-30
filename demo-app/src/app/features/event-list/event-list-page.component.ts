import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatToolbar } from '@angular/material/toolbar';
import { DatePipe } from '@angular/common';
import { ConfirmDeleteDialogComponent } from '../../components/confirm-delete-dialog/confirm-delete-dialog.component';
import {
  EventFormDialogComponent,
  EventFormDialogData,
  EventFormDialogResult,
} from '../../components/event-form-dialog/event-form-dialog.component';
import { Event } from '../../models/event.model';
import { Person } from '../../models/person.model';
import { EventListStore } from './event-list.store';
import { PersonService } from '../../services/person.service';

@Component({
  selector: 'app-event-list-page',
  standalone: true,
  imports: [MatTableModule, MatButtonModule, MatIconModule, MatDialogModule, MatToolbar, DatePipe],
  templateUrl: './event-list-page.component.html',
  styleUrl: './event-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventListPageComponent implements OnInit {
  private readonly dialog = inject(MatDialog);
  private readonly store = inject(EventListStore);
  private readonly personService = inject(PersonService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly events = this.store.events;
  protected readonly hasError = this.store.hasError;
  protected readonly isLoading = this.store.isLoading;
  protected readonly organizers = signal<Person[]>([]);
  protected readonly displayedColumns = ['title', 'location', 'date', 'maxParticipants', 'organizer', 'actions'];

  ngOnInit(): void {
    this.store.load();
    this.personService.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((persons) =>
        this.organizers.set(persons.filter((p) => p.role === 'ORGANIZER'))
      );
  }

  protected openCreateDialog(): void {
    if (this.isLoading()) return;

    this.dialog
      .open<EventFormDialogComponent, EventFormDialogData, EventFormDialogResult>(
        EventFormDialogComponent,
        { data: { title: 'Create Event', submitLabel: 'Create', organizers: this.organizers() } },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;
        this.store.create(result);
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
        { data: { person: { name: event.title } } },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (!confirmed) return;
        this.store.remove(event.id);
      });
  }
}
