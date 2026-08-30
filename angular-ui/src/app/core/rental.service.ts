import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Rental } from './models';

@Injectable({ providedIn: 'root' })
export class RentalService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/rentals`;

  rent(propertyId: number): Observable<Rental> {
    return this.http.post<Rental>(this.baseUrl, { propertyId });
  }

  cancel(id: number): Observable<Rental> {
    return this.http.post<Rental>(`${this.baseUrl}/${id}/cancel`, {});
  }

  getMy(): Observable<Rental[]> {
    return this.http.get<Rental[]>(`${this.baseUrl}/my`);
  }

  getAll(): Observable<Rental[]> {
    return this.http.get<Rental[]>(this.baseUrl);
  }
}
