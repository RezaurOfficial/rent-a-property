import { CanActivateFn } from '@angular/router';
import { AuthGuardData, createAuthGuard } from 'keycloak-angular';

const isAccessAllowed = async (
  _route: unknown,
  state: { url: string },
  authData: AuthGuardData,
): Promise<boolean> => {
  const { authenticated, keycloak } = authData;

  if (authenticated) {
    return true;
  }

  await keycloak.login({ redirectUri: window.location.origin + state.url });
  return false;
};

/** Requires the user to be authenticated; otherwise redirects to Keycloak login. */
export const authGuard: CanActivateFn = createAuthGuard(isAccessAllowed);
