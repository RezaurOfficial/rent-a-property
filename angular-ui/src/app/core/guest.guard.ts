import { createRedirectingGuard } from './redirecting-guard';

/** Blocks pages meant only for anonymous visitors (e.g. registration) once logged in. */
export const guestGuard = createRedirectingGuard((authData) => !authData.authenticated);
