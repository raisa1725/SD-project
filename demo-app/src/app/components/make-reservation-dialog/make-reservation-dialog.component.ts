import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface MakeReservationDialogData {
  eventTitle: string;
  maxSpots: number;
}

export type MakeReservationDialogResult = number | undefined;

@Component({
  selector: 'app-make-reservation-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './make-reservation-dialog.component.html',
  styleUrl: './make-reservation-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MakeReservationDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(
    MatDialogRef<MakeReservationDialogComponent, MakeReservationDialogResult>,
  );

  protected readonly data = inject<MakeReservationDialogData>(MAT_DIALOG_DATA);

  protected readonly form = this.fb.nonNullable.group({
    spotsReserved: [
      1,
      [
        Validators.required,
        Validators.min(1),
        Validators.max(this.data.maxSpots),
      ],
    ],
  });

  protected decreaseSpots(): void {
    const currentValue = Number(this.form.controls.spotsReserved.value);

    if (currentValue <= 1) {
      return;
    }

    this.form.controls.spotsReserved.setValue(currentValue - 1);
  }

  protected increaseSpots(): void {
    const currentValue = Number(this.form.controls.spotsReserved.value);

    if (currentValue >= this.data.maxSpots) {
      return;
    }

    this.form.controls.spotsReserved.setValue(currentValue + 1);
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.dialogRef.close(this.form.controls.spotsReserved.value);
  }

  protected cancel(): void {
    this.dialogRef.close(undefined);
  }
}
