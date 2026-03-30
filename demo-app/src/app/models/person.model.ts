export type PersonRole = 'USER' | 'ORGANIZER';

export interface Person {
  id: string;
  name: string;
  age: number;
  email: string;
  password: string;
  role: PersonRole;
}

export type CreatePersonDto = Omit<Person, 'id'>;
export type UpdatePersonDto = Partial<Omit<Person, 'id'>>;
