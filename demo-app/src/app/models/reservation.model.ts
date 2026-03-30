import { Event } from './event.model';
import { Person } from './person.model';

export type ReservationStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED';

export interface Reservation {
  id: string;
  person: Person;
  event: Event;
  spotsReserved: number;
  status: ReservationStatus;
}

export type CreateReservationDto = {
  personId: string;
  eventId: string;
  spotsReserved: number;
};

export type UpdateReservationDto = {
  spotsReserved?: number;
  status?: ReservationStatus;
};
