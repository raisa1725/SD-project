import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateEventDto, Event, UpdateEventDto } from '../models/event.model';

const API_URL = 'http://localhost:8080/event';

@Injectable({ providedIn: 'root' })
export class EventService {
  private readonly http = inject(HttpClient);

  getAll(): Observable<Event[]> {
    return this.http.get<Event[]>(API_URL);
  }

  getById(id: string): Observable<Event> {
    return this.http.get<Event>(`${API_URL}/${id}`);
  }

  create(dto: CreateEventDto): Observable<Event> {
    return this.http.post<Event>(API_URL, dto);
  }

  update(id: string, dto: CreateEventDto): Observable<Event> {
    return this.http.put<Event>(`${API_URL}/${id}`, dto);
  }

  patch(id: string, dto: UpdateEventDto): Observable<Event> {
    return this.http.patch<Event>(`${API_URL}/${id}`, dto);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`);
  }
}
