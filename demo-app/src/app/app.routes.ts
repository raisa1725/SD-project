import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login',
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/login/login.component').then((m) => m.LoginComponent),
  },

  {
    path: 'admin',
    pathMatch: 'full',
    redirectTo: 'admin/persons',
  },
  {
    path: 'admin/persons',
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () =>
      import('./features/person-list/person-list-page.component').then(
        (m) => m.PersonListPageComponent,
      ),
  },
  {
    path: 'admin/events',
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () =>
      import('./features/event-list/event-list-page.component').then(
        (m) => m.EventListPageComponent,
      ),
  },
  {
    path: 'admin/reservations',
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () =>
      import('./features/reservation-list/reservation-list-page.component').then(
        (m) => m.ReservationListPageComponent,
      ),
  },

  {
    path: 'feed',
    canActivate: [authGuard],
    data: { roles: ['USER', 'ORGANIZER', 'ADMIN'] },
    loadComponent: () =>
      import('./features/event-list/event-list-page.component').then(
        (m) => m.EventListPageComponent,
      ),
  },

  {
    path: 'events',
    redirectTo: 'feed',
    pathMatch: 'full',
  },
  {
    path: 'people',
    redirectTo: 'admin/persons',
    pathMatch: 'full',
  },
  {
    path: 'reservations',
    redirectTo: 'admin/reservations',
    pathMatch: 'full',
  },

  {
    path: 'profile',
    canActivate: [authGuard],
    data: { roles: ['USER', 'ORGANIZER'] },
    loadComponent: () =>
      import('./features/profile/profile-page.component').then(
        ({ ProfilePageComponent }) => ProfilePageComponent,
      ),
  },

  {
    path: 'error',
    loadComponent: () =>
      import('./features/not-found/not-found-page.component').then(
        (m) => m.NotFoundPageComponent,
      ),
  },
  {
    path: '**',
    redirectTo: 'error',
  },
];
