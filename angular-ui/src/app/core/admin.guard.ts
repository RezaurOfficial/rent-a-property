import { createRedirectingGuard } from './redirecting-guard';

/** Requires the user to be authenticated with the ADMIN realm role; otherwise redirects to '/'. */
export const adminGuard = createRedirectingGuard(
  (authData) => authData.authenticated && authData.grantedRoles.realmRoles.includes('ADMIN'),
);
