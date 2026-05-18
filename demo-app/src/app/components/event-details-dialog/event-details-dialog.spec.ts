import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
} from '@angular/material/dialog';

import { EventDetailsDialogComponent } from './event-details-dialog';

describe('EventDetailsDialogComponent', () => {
  let component: EventDetailsDialogComponent;
  let fixture: ComponentFixture<EventDetailsDialogComponent>;

  const dialogRefMock = {
    close: () => {},
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventDetailsDialogComponent],
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            event: {
              id: '1',
              title: 'Mountain Hiking',
              description: 'Beautiful hiking event.',
              location: 'Cluj',
              date: '2030-01-01T10:00:00',
              maxParticipants: 20,
              organizer: {
                id: 'org-1',
                name: 'John Organizer',
                email: 'john@test.com',
              },
            },
            canReserve: true,
          },
        },
        {
          provide: MatDialogRef,
          useValue: dialogRefMock,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EventDetailsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
