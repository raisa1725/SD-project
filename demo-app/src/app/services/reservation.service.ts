import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateReservationDto, Reservation, UpdateReservationDto } from '../models/reservation.model';

const API_URL = 'http://localhost:8080/reservation';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private readonly http = inject(HttpClient);

  getAll(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(API_URL);
  }

  getById(id: string): Observable<Reservation> {
    return this.http.get<Reservation>(`${API_URL}/${id}`);
  }

  create(dto: CreateReservationDto): Observable<Reservation> {
    return this.http.post<Reservation>(API_URL, dto);
  }

  update(id: string, dto: UpdateReservationDto): Observable<Reservation> {
    return this.http.put<Reservation>(`${API_URL}/${id}`, dto);
  }

  patch(id: string, dto: UpdateReservationDto): Observable<Reservation> {
    return this.http.patch<Reservation>(`${API_URL}/${id}`, dto);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`);
  }
}
