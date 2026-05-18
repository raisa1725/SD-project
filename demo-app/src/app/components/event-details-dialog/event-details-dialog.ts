import { DatePipe } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';

import { Event } from '../../models/event.model';

export interface EventDetailsDialogData {
  event: Event;
  canReserve: boolean;
}

export type EventDetailsDialogResult = 'reserve' | undefined;

@Component({
  selector: 'app-event-details-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    DatePipe,
  ],
  templateUrl: './event-details-dialog.html',
  styleUrl: './event-details-dialog.scss',
})
export class EventDetailsDialogComponent {
  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: EventDetailsDialogData,
    private readonly dialogRef: MatDialogRef<
      EventDetailsDialogComponent,
      EventDetailsDialogResult
    >,
  ) {}

  protected reserve(): void {
    this.dialogRef.close('reserve');
  }
}
