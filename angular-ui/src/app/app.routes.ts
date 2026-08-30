import { Routes } from '@angular/router';

import { adminGuard } from './core/admin.guard';
import { authGuard } from './core/auth.guard';
import { guestGuard } from './core/guest.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./properties/property-list/property-list').then((m) => m.PropertyList),
  },
  {
    path: 'register',
    loadComponent: () => import('./auth/register/register').then((m) => m.Register),
    canActivate: [guestGuard],
  },
  {
    path: 'my-rentals',
    loadComponent: () => import('./rentals/my-rentals/my-rentals').then((m) => m.MyRentals),
    canActivate: [authGuard],
  },
  {
    path: 'admin/properties',
    loadComponent: () =>
      import('./admin/property-admin-list/property-admin-list').then(
        (m) => m.PropertyAdminList,
      ),
    canActivate: [adminGuard],
  },
  {
    path: 'admin/rentals',
    loadComponent: () =>
      import('./admin/rental-admin-list/rental-admin-list').then((m) => m.RentalAdminList),
    canActivate: [adminGuard],
  },
  {
    path: 'admin/properties/new',
    loadComponent: () =>
      import('./admin/property-form/property-form').then((m) => m.PropertyForm),
    canActivate: [adminGuard],
  },
  {
    path: 'admin/properties/:id/edit',
    loadComponent: () =>
      import('./admin/property-form/property-form').then((m) => m.PropertyForm),
    canActivate: [adminGuard],
  },
  { path: '**', redirectTo: '' },
];
