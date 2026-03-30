import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Person } from '../../models/person.model';
import { Event } from '../../models/event.model';
import { CreateReservationDto, ReservationStatus, UpdateReservationDto } from '../../models/reservation.model';

export interface ReservationFormDialogData {
  title: string;
  submitLabel?: string;
  persons: Person[];
  events: Event[];
  isEdit?: boolean;
  initialValue?: {
    personId?: string;
    eventId?: string;
    spotsReserved?: number;
    status?: ReservationStatus;
  } | null;
}

export type ReservationFormDialogResult = CreateReservationDto | UpdateReservationDto | undefined;

@Component({
  selector: 'app-reservation-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
  ],
  templateUrl: './reservation-form-dialog.component.html',
  styleUrl: './reservation-form-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReservationFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<ReservationFormDialogComponent>);
  protected readonly data = inject<ReservationFormDialogData>(MAT_DIALOG_DATA);

  protected readonly statuses: ReservationStatus[] = ['PENDING', 'ACCEPTED', 'DECLINED'];

  protected readonly form = this.fb.nonNullable.group({
    personId: ['', [Validators.required]],
    eventId: ['', [Validators.required]],
    spotsReserved: [1, [Validators.required, Validators.min(1)]],
    status: ['PENDING' as ReservationStatus, [Validators.required]],
  });

  ngOnInit(): void {
    if (this.data.initialValue) {
      this.form.patchValue(this.data.initialValue);
    }

    if (this.data.isEdit) {
      this.form.controls.personId.disable();
      this.form.controls.eventId.disable();
    }
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { personId, eventId, spotsReserved, status } = this.form.getRawValue();

    if (this.data.isEdit) {
      this.dialogRef.close({ spotsReserved, status } as UpdateReservationDto);
    } else {
      this.dialogRef.close({ personId, eventId, spotsReserved } as CreateReservationDto);
    }
  }

  protected cancel(): void {
    this.dialogRef.close(undefined);
  }
}
