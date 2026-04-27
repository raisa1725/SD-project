export type PersonRole = 'USER' | 'ORGANIZER' | 'ADMIN';

export interface Person {
  id: string;
  name: string;
  age: number;
  email: string;
  password: string;
  role: PersonRole;
}

export interface CreatePersonDto {
  name: string;
  age: number;
  email: string;
  password: string;
  role: PersonRole;
}

export interface UpdatePersonDto {
  name?: string;
  age?: number;
  email?: string;
  password?: string;
  role?: PersonRole;
}
