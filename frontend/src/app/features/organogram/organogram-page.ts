import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService } from '../../core/api.service';
import { OrgUnitNode, OrgUnitPostView, OrgUnitView } from '../../core/models';
import { confirm } from '../../shared/confirm-dialog';
import { TreeView, expandToDepth } from '../../shared/tree-view';
import { OrgChart } from './org-chart';
import { OrgUnitFormDialog } from './org-unit-form-dialog';

/** Organogram: org unit chart/tree, unit details and sanctioned posts (filled / vacancy). */
@Component({
  selector: 'app-organogram-page',
  imports: [OrgChart, TreeView, RouterLink, FormsModule, MatButtonModule, MatButtonToggleModule, MatIconModule,
            MatFormFieldModule, MatInputModule, MatSelectModule, MatTooltipModule],
  template: `
    <div class="page-header">
      <h1>Organogram</h1>
      <span class="badge FUNCTIONAL">FUNCTIONAL</span>
      <span class="badge NON_FUNCTIONAL">NON_FUNCTIONAL</span>
      <mat-button-toggle-group [(ngModel)]="view" hideSingleSelectionIndicator aria-label="View">
        <mat-button-toggle value="chart"><mat-icon>account_tree</mat-icon> Chart</mat-button-toggle>
        <mat-button-toggle value="tree"><mat-icon>list</mat-icon> Tree</mat-button-toggle>
      </mat-button-toggle-group>
      <button mat-flat-button (click)="openForm(null, null)"><mat-icon>add</mat-icon> Top-level unit</button>
    </div>

    <div class="layout">
      <div class="card chart-card">
        @if (!tree().length) {
          <p class="muted">No org units yet.</p>
        } @else if (view === 'chart') {
          <app-org-chart [nodes]="tree()" [(selectedId)]="selectedId" />
        } @else {
          <ng-template #label let-node>
            <span class="name">{{ node.name }}</span>
            <span class="badge type">{{ node.typeName }}</span>
            @if (node.nature) { <span class="badge" [class]="node.nature">{{ node.nature }}</span> }
          </ng-template>
          <app-tree-view [nodes]="tree()" [nodeTemplate]="label" [(selectedId)]="selectedId" [(expanded)]="expanded" />
        }
      </div>

      <div class="card detail-card">
        @if (unit(); as u) {
          <div class="detail-head">
            <h2>{{ u.name }}</h2>
            <span class="badge type">{{ u.typeName }} · L{{ u.levelNo }}</span>
            @if (u.nature) { <span class="badge" [class]="u.nature">{{ u.nature }}</span> }
            <span class="badge" [class]="u.status">{{ u.status }}</span>
          </div>
          <dl>
            <dt>Code</dt><dd>{{ u.code }}</dd>
            <dt>Parent</dt><dd>{{ u.parentName ?? '— (top level)' }}</dd>
            <dt>Location</dt><dd>{{ u.locationName ?? '—' }}</dd>
          </dl>
          <div class="row">
            <button mat-stroked-button (click)="openForm(null, u.id)"><mat-icon>add</mat-icon> Add sub-unit</button>
            <button mat-stroked-button (click)="openForm(u, null)"><mat-icon>edit</mat-icon> Edit / move</button>
            <button mat-stroked-button (click)="remove(u)"><mat-icon>delete</mat-icon> Delete</button>
            <a mat-button [routerLink]="['/employees']" [queryParams]="{ orgUnitId: u.id }">
              <mat-icon>group</mat-icon> Employees
            </a>
          </div>

          <h3>Sanctioned posts</h3>
          <table class="posts">
            <thead>
              <tr><th>Designation</th><th class="num">Sanctioned</th><th class="num">Filled</th><th class="num">Vacancy</th><th></th></tr>
            </thead>
            <tbody>
              @for (p of posts(); track p.id) {
                <tr>
                  <td>{{ p.designationName }} <span class="muted grade">{{ p.gradeCode }}</span></td>
                  <td class="num">
                    <input class="count" type="number" min="0" [value]="p.sanctioned"
                           (change)="updateCount(p, $any($event.target).valueAsNumber)" [attr.aria-label]="'Sanctioned ' + p.designationName" />
                  </td>
                  <td class="num">{{ p.filled }}</td>
                  <td class="num"><span class="vac" [class.neg]="p.vacancy < 0" [class.pos]="p.vacancy > 0">{{ p.vacancy }}</span></td>
                  <td><button mat-icon-button matTooltip="Remove line" (click)="removePost(p)"><mat-icon>close</mat-icon></button></td>
                </tr>
              } @empty {
                <tr><td colspan="5" class="muted">No sanctioned posts defined.</td></tr>
              }
            </tbody>
            @if (posts().length) {
              <tfoot>
                <tr><td>Total</td><td class="num">{{ totals().sanctioned }}</td><td class="num">{{ totals().filled }}</td>
                    <td class="num">{{ totals().sanctioned - totals().filled }}</td><td></td></tr>
              </tfoot>
            }
          </table>

          <div class="row add-post">
            <mat-form-field>
              <mat-label>Designation</mat-label>
              <mat-select [(ngModel)]="newDesignationId">
                @for (d of freeDesignations(); track d.id) { <mat-option [value]="d.id">{{ d.name }} · {{ d.gradeCode }}</mat-option> }
              </mat-select>
            </mat-form-field>
            <mat-form-field class="count-field">
              <mat-label>Posts</mat-label>
              <input matInput type="number" min="0" [(ngModel)]="newCount" />
            </mat-form-field>
            <button mat-stroked-button [disabled]="!newDesignationId" (click)="addPost(u.id)"><mat-icon>add</mat-icon> Add</button>
          </div>
        } @else {
          <p class="muted">Click a unit in the chart to see its details and sanctioned posts.</p>
        }
      </div>
    </div>
  `,
  styles: `
    .layout { display: grid; grid-template-columns: 1fr 380px; gap: 16px; align-items: start; }
    .chart-card { overflow: auto; max-height: calc(100vh - 170px); min-height: 300px; }
    .name { font-weight: 500; }
    .detail-head { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; h2 { margin: 0; font: var(--mat-sys-title-large); } }
    dl { display: grid; grid-template-columns: 80px 1fr; gap: 6px 12px; margin: 16px 0; }
    dt { color: var(--mat-sys-on-surface-variant); }
    dd { margin: 0; }
    h3 { font: var(--mat-sys-title-small); margin: 20px 0 8px; }
    table.posts { width: 100%; border-collapse: collapse; }
    .posts th, .posts td { padding: 4px 6px; border-bottom: 1px solid var(--mat-sys-outline-variant); text-align: left; }
    .posts th { font: var(--mat-sys-label-medium); color: var(--mat-sys-on-surface-variant); }
    .posts tfoot td { font-weight: 500; border-bottom: 0; }
    .num { text-align: right !important; }
    .grade { font: var(--mat-sys-body-small); }
    input.count { width: 56px; text-align: right; padding: 4px; border: 1px solid var(--mat-sys-outline-variant); border-radius: 6px; font: inherit; }
    .vac { padding: 1px 8px; border-radius: 8px; }
    .vac.pos { background: #e3f4e8; color: #1b6b37; }
    .vac.neg { background: #f3e5e5; color: #8c1d18; }
    .add-post { margin-top: 12px; flex-wrap: nowrap; mat-form-field { width: 160px; } .count-field { width: 80px; } }
    @media (max-width: 1100px) { .layout { grid-template-columns: 1fr; } }
  `,
})
export class OrganogramPage {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);

  protected view: 'chart' | 'tree' = 'chart';
  protected readonly tree = signal<OrgUnitNode[]>([]);
  protected readonly expanded = signal<Set<number>>(new Set());
  protected readonly selectedId = signal<number | null>(null);
  protected readonly unit = signal<OrgUnitView | null>(null);
  protected readonly posts = signal<OrgUnitPostView[]>([]);
  protected readonly designations = toSignal(this.api.designations(), { initialValue: [] });

  protected newDesignationId: number | null = null;
  protected newCount = 1;

  protected readonly totals = computed(() => this.posts().reduce(
    (t, p) => ({ sanctioned: t.sanctioned + p.sanctioned, filled: t.filled + p.filled }), { sanctioned: 0, filled: 0 }));

  /** Designations not yet on this unit's post list. */
  protected readonly freeDesignations = computed(() => {
    const used = new Set(this.posts().map(p => p.designationId));
    return this.designations().filter(d => d.status === 'ACTIVE' && !used.has(d.id));
  });

  constructor() {
    this.reloadTree(true);
    // whenever the selection changes (chart or tree), load the unit and its posts
    effect(() => {
      const id = this.selectedId();
      if (id === null) { this.unit.set(null); this.posts.set([]); return; }
      this.loadUnit(id);
    });
  }

  private reloadTree(firstLoad = false): void {
    this.api.orgUnitTree().subscribe(t => {
      this.tree.set(t);
      if (firstLoad) this.expanded.set(expandToDepth(t, 3));
    });
  }

  private loadUnit(id: number): void {
    this.api.orgUnit(id).subscribe({ next: u => this.unit.set(u), error: () => this.unit.set(null) });
    this.loadPosts(id);
  }

  private loadPosts(id: number): void {
    this.api.posts(id).subscribe(p => this.posts.set(p));
  }

  protected openForm(unit: OrgUnitView | null, parentId: number | null): void {
    this.dialog.open(OrgUnitFormDialog, { data: { unit, parentId, tree: this.tree() }, width: '640px' })
      .afterClosed().subscribe(saved => {
        if (!saved) return;
        this.reloadTree();
        const id = this.selectedId();
        if (id !== null) this.loadUnit(id);
      });
  }

  protected remove(u: OrgUnitView): void {
    confirm(this.dialog, { title: 'Delete org unit', message: `Delete ${u.name}? This fails if it has sub-units, posts or employees.` })
      .subscribe(ok => {
        if (!ok) return;
        this.api.deleteOrgUnit(u.id).subscribe(() => {
          this.selectedId.set(null);
          this.reloadTree();
        });
      });
  }

  protected addPost(unitId: number): void {
    if (!this.newDesignationId) return;
    this.api.savePost(unitId, this.newDesignationId, this.newCount ?? 0).subscribe(() => {
      this.newDesignationId = null;
      this.newCount = 1;
      this.loadPosts(unitId);
    });
  }

  protected updateCount(p: OrgUnitPostView, count: number): void {
    if (Number.isNaN(count) || count === p.sanctioned) return;
    this.api.savePost(p.orgUnitId, p.designationId, count).subscribe({
      next: () => this.loadPosts(p.orgUnitId),
      error: () => this.loadPosts(p.orgUnitId),   // revert the input to the saved value
    });
  }

  protected removePost(p: OrgUnitPostView): void {
    this.api.deletePost(p.id).subscribe(() => this.loadPosts(p.orgUnitId));
  }
}
