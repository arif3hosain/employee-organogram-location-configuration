import { Routes } from '@angular/router';

// Lazy-loaded feature routes (guide §2.2: standalone components, lazy feature routes).
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  {
    path: 'employees',
    title: 'Employees',
    loadComponent: () => import('./features/employees/employee-list').then(m => m.EmployeeList),
  },
  {
    path: 'organogram',
    title: 'Organogram',
    loadComponent: () => import('./features/organogram/organogram-page').then(m => m.OrganogramPage),
  },
  {
    path: 'locations',
    title: 'Locations',
    loadComponent: () => import('./features/locations/locations-page').then(m => m.LocationsPage),
  },
  {
    path: 'setup',
    title: 'Setup',
    loadComponent: () => import('./features/setup/setup-page').then(m => m.SetupPage),
  },
  { path: '**', redirectTo: 'employees' },
];
