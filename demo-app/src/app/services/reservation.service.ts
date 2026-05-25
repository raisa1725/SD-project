import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateReservationDto,
  Reservation,
  UpdateReservationDto,
} from '../models/reservation.model';

const API_URL = 'http://localhost:8080/reservation';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private readonly http = inject(HttpClient);

  private getAuthHeaders(): HttpHeaders {
    const token = sessionStorage.getItem('token');

    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }

  getAll(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(API_URL, {
      headers: this.getAuthHeaders(),
    });
  }

  getById(id: string): Observable<Reservation> {
    return this.http.get<Reservation>(`${API_URL}/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }

  create(dto: CreateReservationDto): Observable<Reservation> {
    return this.http.post<Reservation>(API_URL, dto, {
      headers: this.getAuthHeaders(),
    });
  }

  update(id: string, dto: UpdateReservationDto): Observable<Reservation> {
    return this.http.put<Reservation>(`${API_URL}/${id}`, dto, {
      headers: this.getAuthHeaders(),
    });
  }

  patch(id: string, dto: UpdateReservationDto): Observable<Reservation> {
    return this.http.patch<Reservation>(`${API_URL}/${id}`, dto, {
      headers: this.getAuthHeaders(),
    });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }

  getPendingForOrganizer(organizerId: string): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(
      `${API_URL}/organizer/${organizerId}/pending`,
      {
        headers: this.getAuthHeaders(),
      }
    );
  }

  getAllForOrganizer(organizerId: string): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(
      `${API_URL}/organizer/${organizerId}/all`,
      {
        headers: this.getAuthHeaders(),
      }
    );
  }

  accept(id: string): Observable<Reservation> {
    return this.http.patch<Reservation>(
      `${API_URL}/${id}/accept`,
      {},
      {
        headers: this.getAuthHeaders(),
      }
    );
  }

  decline(id: string): Observable<Reservation> {
    return this.http.patch<Reservation>(
      `${API_URL}/${id}/decline`,
      {},
      {
        headers: this.getAuthHeaders(),
      }
    );
  }
}
