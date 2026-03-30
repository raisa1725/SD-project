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
import { CreateEventDto } from '../../models/event.model';

export interface EventFormDialogData {
  title: string;
  submitLabel?: string;
  organizers: Person[];
  initialValue?: Partial<CreateEventDto> | null;
}

export type EventFormDialogResult = CreateEventDto | undefined;

@Component({
  selector: 'app-event-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
  ],
  templateUrl: './event-form-dialog.component.html',
  styleUrl: './event-form-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<EventFormDialogComponent>);
  protected readonly data = inject<EventFormDialogData>(MAT_DIALOG_DATA);

  protected readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    description: ['', [Validators.maxLength(500)]],
    location: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    date: ['', [Validators.required]],
    maxParticipants: [1, [Validators.required, Validators.min(1)]],
    organizerId: ['', [Validators.required]],
  });

  ngOnInit(): void {
    if (this.data.initialValue) {
      const v = this.data.initialValue;
      this.form.patchValue({
        title: v.title ?? '',
        description: v.description ?? '',
        location: v.location ?? '',
        date: v.date ? this.toDatetimeLocal(v.date) : '',
        maxParticipants: v.maxParticipants ?? 1,
        organizerId: v.organizerId ?? '',
      });
    }
  }

  private toDatetimeLocal(isoString: string): string {
    const d = new Date(isoString);
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { title, description, location, date, maxParticipants, organizerId } =
      this.form.getRawValue();

    this.dialogRef.close({
      title,
      description: description || undefined,
      location,
      date: new Date(date).toISOString(),
      maxParticipants,
      organizerId,
    } as CreateEventDto);
  }

  protected cancel(): void {
    this.dialogRef.close(undefined);
  }
}
