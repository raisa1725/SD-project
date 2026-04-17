import { Routes } from '@angular/router';
// @ts-ignore
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
    path: 'people',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/person-list/person-list-page.component').then(
        (m) => m.PersonListPageComponent,
      ),
  },
  {
    path: 'events',
    loadComponent: () =>
      import('./features/event-list/event-list-page.component').then(
        (m) => m.EventListPageComponent,
      ),
  },
  {
    path: 'reservations',
    loadComponent: () =>
      import('./features/reservation-list/reservation-list-page.component').then(
        (m) => m.ReservationListPageComponent,
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
