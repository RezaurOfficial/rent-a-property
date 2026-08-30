import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { UserSummary } from './models';

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

/**
 * REST calls hosted directly on the gateway rather than proxied to a business
 * service: public self-registration, and (ADMIN-only) live Keycloak user lookups.
 */
@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly authBaseUrl = `${environment.apiBaseUrl}/auth`;
  private readonly adminBaseUrl = `${environment.apiBaseUrl}/admin`;

  register(payload: RegisterRequest): Observable<unknown> {
    return this.http.post(`${this.authBaseUrl}/register`, payload);
  }

  getUser(userId: string): Observable<UserSummary> {
    return this.http.get<UserSummary>(`${this.adminBaseUrl}/users/${userId}`);
  }
}
