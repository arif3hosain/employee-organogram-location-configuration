import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { ApiService } from '../../core/api.service';
import { LocationNode, LocationView, countryHeading } from '../../core/models';
import { confirm } from '../../shared/confirm-dialog';
import { TreeView, expandToDepth } from '../../shared/tree-view';
import { ancestorIds, groupByCountry, isCountryHeading } from '../../shared/tree-utils';
import { LocationFormDialog } from './location-form-dialog';

/**
 * Type-wise location tree (replaces Division / District / Upazila). Country is its own table
 * (Setup › Countries); each country's location types define its levels. With several countries
 * the tree is grouped under a heading per country.
 */
@Component({
  selector: 'app-locations-page',
  imports: [TreeView, RouterLink, MatButtonModule, MatIconModule],
  template: `
    <div class="page-header">
      <h1>Locations</h1>
      <span class="muted">Types are configurable in Setup. The tree below follows them.</span>
      <button mat-flat-button (click)="openForm(null, null)"><mat-icon>add</mat-icon> Top-level location</button>
    </div>

    <div class="layout">
      <div class="card tree-card">
        <ng-template #label let-node>
          <span class="name">{{ node.name }}</span>
          <span class="muted code">{{ node.code }}</span>
          <span class="badge type">{{ node.typeName }}</span>
          @if (node.status === 'INACTIVE') { <span class="badge INACTIVE">INACTIVE</span> }
        </ng-template>
        @if (tree().length) {
          <app-tree-view [nodes]="tree()" [nodeTemplate]="label" [(selectedId)]="selectedId" [(expanded)]="expanded"
                         (picked)="select($event.id)" />
        } @else {
          <p class="muted">No locations yet.</p>
        }
      </div>

      <div class="card detail-card">
        @if (selected(); as l) {
          <div class="detail-head">
            <h2>{{ l.name }}</h2>
            <span class="badge type">{{ l.typeName }} · L{{ l.levelNo }}</span>
            <span class="badge" [class]="l.status">{{ l.status }}</span>
          </div>
          <dl>
            <dt>Code</dt><dd>{{ l.code }}</dd>
            <dt>Country</dt><dd>{{ l.countryName }} ({{ l.countryCode }})</dd>
            <dt>Parent</dt><dd>{{ l.parentName ?? '— (top level)' }}</dd>
            <dt>Path</dt><dd><code>{{ l.path }}</code></dd>
            <dt>Version</dt><dd>{{ l.version }}</dd>
          </dl>
          <div class="row">
            <button mat-stroked-button (click)="openForm(null, l.id)"><mat-icon>add</mat-icon> Add child</button>
            <button mat-stroked-button (click)="openForm(l, null)"><mat-icon>edit</mat-icon> Edit / move</button>
            <button mat-stroked-button (click)="remove(l)"><mat-icon>delete</mat-icon> Delete</button>
            <a mat-button [routerLink]="['/employees']" [queryParams]="{ locationId: l.id }">
              <mat-icon>group</mat-icon> Employees here and below
            </a>
          </div>
        } @else {
          <p class="muted">Select a location to see details.</p>
        }
      </div>
    </div>
  `,
  styles: `
    .layout { display: grid; grid-template-columns: minmax(360px, 1fr) minmax(320px, 1fr); gap: 16px; align-items: start; }
    .tree-card { max-height: calc(100vh - 170px); overflow: auto; }
    .name { font-weight: 500; }
    .code { font: var(--mat-sys-body-small); }
    .detail-head { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; h2 { margin: 0; font: var(--mat-sys-title-large); } }
    dl { display: grid; grid-template-columns: 90px 1fr; gap: 6px 12px; margin: 16px 0; }
    dt { color: var(--mat-sys-on-surface-variant); }
    dd { margin: 0; }
  `,
})
export class LocationsPage {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);

  protected readonly tree = signal<LocationNode[]>([]);
  protected readonly expanded = signal<Set<number>>(new Set());
  protected readonly selectedId = signal<number | null>(null);
  protected readonly selected = signal<LocationView | null>(null);

  constructor() {
    this.reload(true);
  }

  private reload(firstLoad = false): void {
    this.api.locationTree().subscribe(t => {
      t = groupByCountry(t, countryHeading);
      const newHeadings = t.filter(n => isCountryHeading(n.id) && !this.tree().some(o => o.id === n.id)).map(n => n.id);
      this.tree.set(t);
      if (firstLoad) this.expanded.set(expandToDepth(t, 2));
      else if (newHeadings.length) this.expanded.update(s => new Set([...s, ...newHeadings]));   // a 2nd country was just added
      const id = this.selectedId();
      if (id !== null) {
        // keep the selection visible after a move
        const trail = ancestorIds(t, id);
        if (trail) this.expanded.update(s => new Set([...s, ...trail]));
        this.select(id);
      }
    });
  }

  protected select(id: number): void {
    if (isCountryHeading(id)) {      // a country heading only opens / closes
      this.expanded.update(s => { const n = new Set(s); if (n.has(id)) n.delete(id); else n.add(id); return n; });
      this.selectedId.set(this.selected()?.id ?? null);
      return;
    }
    this.selectedId.set(id);
    this.api.location(id).subscribe({ next: l => this.selected.set(l), error: () => this.selected.set(null) });
  }

  protected openForm(location: LocationView | null, parentId: number | null): void {
    this.dialog.open(LocationFormDialog, { data: { location, parentId, tree: this.tree() }, width: '640px' })
      .afterClosed().subscribe(saved => {
        if (!saved) return;
        if (parentId) this.expanded.update(s => new Set(s).add(parentId));
        this.reload();
      });
  }

  protected remove(l: LocationView): void {
    confirm(this.dialog, { title: 'Delete location', message: `Delete ${l.name}? This fails if it has children or is in use.` })
      .subscribe(ok => {
        if (!ok) return;
        this.api.deleteLocation(l.id).subscribe(() => {
          this.selectedId.set(null);
          this.selected.set(null);
          this.reload();
        });
      });
  }
}
