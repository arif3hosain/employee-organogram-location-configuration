import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatIconModule, MatIconRegistry } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatToolbarModule, MatSidenavModule, MatListModule, MatIconModule],
  template: `
    <mat-toolbar class="topbar">
      <mat-icon>insights</mat-icon>
      <span class="title">PMS POC</span>
      <span class="sub">core HR: employees · organogram · locations</span>
    </mat-toolbar>

    <mat-sidenav-container class="shell">
      <mat-sidenav mode="side" opened class="nav">
        <mat-nav-list>
          @for (item of nav; track item.path) {
            <a mat-list-item [routerLink]="item.path" routerLinkActive #rla="routerLinkActive" [activated]="rla.isActive">
              <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
              <span matListItemTitle>{{ item.label }}</span>
            </a>
          }
        </mat-nav-list>
      </mat-sidenav>
      <mat-sidenav-content class="content">
        <router-outlet />
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: `
    :host { display: flex; flex-direction: column; height: 100vh; }
    .topbar { gap: 12px; background: var(--mat-sys-primary); color: var(--mat-sys-on-primary); }
    .title { font-weight: 500; }
    .sub { font: var(--mat-sys-body-small); opacity: .8; }
    .shell { flex: 1; }
    .nav { width: 210px; border-right: 1px solid var(--mat-sys-outline-variant); }
    .content { padding: 20px 24px; box-sizing: border-box; }
  `,
})
export class App {
  protected readonly nav = [
    { path: '/employees', label: 'Employees', icon: 'badge' },
    { path: '/organogram', label: 'Organogram', icon: 'account_tree' },
    { path: '/locations', label: 'Locations', icon: 'location_on' },
    { path: '/setup', label: 'Setup', icon: 'tune' },
  ];

  constructor() {
    // index.html loads the "Material Symbols Outlined" font
    inject(MatIconRegistry).setDefaultFontSetClass('material-symbols-outlined');
  }
}
