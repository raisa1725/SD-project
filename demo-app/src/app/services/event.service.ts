import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
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

  search(params: {
    title?: string;
    location?: string;
    upcoming?: boolean;
  }): Observable<Event[]> {
    let httpParams = new HttpParams();

    if (params.title?.trim()) {
      httpParams = httpParams.set('title', params.title.trim());
    }

    if (params.location?.trim()) {
      httpParams = httpParams.set('location', params.location.trim());
    }

    if (params.upcoming !== undefined) {
      httpParams = httpParams.set('upcoming', params.upcoming);
    }

    return this.http.get<Event[]>(`${API_URL}/search`, { params: httpParams });
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
