import { HttpInterceptorFn } from '@angular/common/http';
import {
  createInterceptorCondition,
  includeBearerTokenInterceptor,
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  IncludeBearerTokenCondition,
} from 'keycloak-angular';

import { environment } from '../../environments/environment';

/**
 * Functional interceptor that attaches `Authorization: Bearer <token>` to outgoing
 * requests when a Keycloak token is available. Backed by keycloak-angular's
 * `includeBearerTokenInterceptor`, configured below to only match our API gateway.
 */
export const authInterceptor: HttpInterceptorFn = includeBearerTokenInterceptor;

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/**
 * Provider for `INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG`, restricting the bearer
 * token to requests aimed at our API gateway (never third-party URLs). The gateway's
 * public endpoints (e.g. `/api/auth/register`) live on their own security filter
 * chain with no OAuth2 resource server attached, so a token here — stale or not —
 * can never affect them; no client-side carve-out is needed.
 */
export const authInterceptorConfigProvider = {
  provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  useValue: [
    createInterceptorCondition<IncludeBearerTokenCondition>({
      urlPattern: new RegExp(`^${escapeRegExp(environment.apiBaseUrl)}(/.*)?$`),
    }),
  ],
};
