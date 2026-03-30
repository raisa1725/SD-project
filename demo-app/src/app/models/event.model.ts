import { Person } from './person.model';

export interface Event {
  id: string;
  title: string;
  description?: string;
  location: string;
  date: string;
  maxParticipants: number;
  organizer: Person;
}

export type CreateEventDto = {
  title: string;
  description?: string;
  location: string;
  date: string;
  maxParticipants: number;
  organizerId: string;
};

export type UpdateEventDto = Partial<CreateEventDto>;
