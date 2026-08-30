import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthGuardData, createAuthGuard } from 'keycloak-angular';

/**
 * Builds a route guard that allows navigation when `predicate(authData)` is true,
 * otherwise redirects to '/'.
 */
export function createRedirectingGuard(predicate: (authData: AuthGuardData) => boolean): CanActivateFn {
  return createAuthGuard(
    async (_route: unknown, _state: unknown, authData: AuthGuardData): Promise<boolean | UrlTree> => {
      // inject() must be called synchronously (before any `await`) to stay within
      // the router's injection context.
      const router = inject(Router);
      return predicate(authData) ? true : router.parseUrl('/');
    },
  );
}
