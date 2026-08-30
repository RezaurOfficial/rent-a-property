import { Injectable, computed, inject } from '@angular/core';
import Keycloak from 'keycloak-js';
import { KEYCLOAK_EVENT_SIGNAL } from 'keycloak-angular';

/**
 * Thin wrapper around the injected Keycloak instance, exposing both reactive
 * signals (for templates) and plain methods (for guards / one-off checks).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly keycloak = inject(Keycloak);
  private readonly keycloakEvent = inject(KEYCLOAK_EVENT_SIGNAL);

  /** Reactive signal reflecting the current authentication state. */
  readonly authenticated = computed(() => {
    this.keycloakEvent();
    return !!this.keycloak.authenticated;
  });

  /** Reactive signal reflecting whether the current user has the ADMIN role. */
  readonly isAdmin = computed(() => this.hasRole('ADMIN'));

  /** Reactive signal with the logged-in user's display name. */
  readonly displayName = computed(() => {
    this.keycloakEvent();
    return this.keycloak.tokenParsed?.['preferred_username'] as string | undefined;
  });

  login(): void {
    void this.keycloak.login();
  }

  logout(): void {
    void this.keycloak.logout({ redirectUri: window.location.origin });
  }

  hasRole(role: string): boolean {
    this.keycloakEvent();
    const roles = this.keycloak.tokenParsed?.realm_access?.roles ?? [];
    return roles.includes(role);
  }
}
