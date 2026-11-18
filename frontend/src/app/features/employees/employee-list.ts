import { Component, OnInit, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { debounceTime } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Lookups } from '../../core/lookups';
import { EmployeeView, RecordStatus } from '../../core/models';
import { confirm } from '../../shared/confirm-dialog';
import { LocationPicker } from '../../shared/location-picker';
import { indent } from '../../shared/tree-utils';
import { EmployeeFormDialog } from './employee-form-dialog';

@Component({
  selector: 'app-employee-list',
  imports: [ReactiveFormsModule, MatTableModule, MatPaginatorModule, MatButtonModule, MatIconModule,
            MatFormFieldModule, MatInputModule, MatSelectModule, MatTooltipModule, MatProgressBarModule, LocationPicker],
  template: `
    <div class="page-header">
      <h1>Employees</h1>
      <button mat-flat-button (click)="openForm(null)"><mat-icon>person_add</mat-icon> New employee</button>
    </div>

    <form [formGroup]="filters" class="row filters">
      <mat-form-field class="grow">
        <mat-label>Search code, name or email</mat-label>
        <mat-icon matPrefix>search</mat-icon>
        <input matInput formControlName="q" />
      </mat-form-field>
      <mat-form-field>
        <mat-label>Org unit (incl. sub-units)</mat-label>
        <mat-select formControlName="orgUnitId">
          <mat-option [value]="null">All</mat-option>
          @for (o of orgUnits(); track o.id) {
            <mat-option [value]="o.id">{{ indent(o.depth) }}{{ o.label }}</mat-option>
          }
        </mat-select>
      </mat-form-field>
      <app-location-picker class="loc" formControlName="locationId" variant="filter" label="Location (incl. below)" />
      <mat-form-field class="narrow">
        <mat-label>Designation</mat-label>
        <mat-select formControlName="designationId">
          <mat-option [value]="null">All</mat-option>
          @for (d of designations(); track d.id) { <mat-option [value]="d.id">{{ d.name }} · {{ d.gradeCode }}</mat-option> }
        </mat-select>
      </mat-form-field>
      <mat-form-field class="narrow">
        <mat-label>Status</mat-label>
        <mat-select formControlName="status">
          <mat-option [value]="null">All</mat-option>
          <mat-option value="ACTIVE">ACTIVE</mat-option>
          <mat-option value="INACTIVE">INACTIVE</mat-option>
        </mat-select>
      </mat-form-field>
    </form>

    <div class="card table-card">
      @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
      <table mat-table [dataSource]="rows()">
        <ng-container matColumnDef="employeeCode">
          <th mat-header-cell *matHeaderCellDef>Code</th>
          <td mat-cell *matCellDef="let e"><strong>{{ e.employeeCode }}</strong></td>
        </ng-container>
        <ng-container matColumnDef="fullName">
          <th mat-header-cell *matHeaderCellDef>Name</th>
          <td mat-cell *matCellDef="let e">{{ e.fullName }}<div class="muted small">{{ e.email }}</div></td>
        </ng-container>
        <ng-container matColumnDef="designationName">
          <th mat-header-cell *matHeaderCellDef>Designation</th>
          <td mat-cell *matCellDef="let e">{{ e.designationName }}<div class="muted small">{{ e.gradeName }}</div></td>
        </ng-container>
        <ng-container matColumnDef="orgUnitName">
          <th mat-header-cell *matHeaderCellDef>Org unit</th>
          <td mat-cell *matCellDef="let e">{{ e.orgUnitName }}</td>
        </ng-container>
        <ng-container matColumnDef="locationName">
          <th mat-header-cell *matHeaderCellDef>Location</th>
          <td mat-cell *matCellDef="let e">{{ e.locationName }}</td>
        </ng-container>
        <ng-container matColumnDef="supervisorName">
          <th mat-header-cell *matHeaderCellDef>Supervisor</th>
          <td mat-cell *matCellDef="let e">{{ e.supervisorName ?? '—' }}</td>
        </ng-container>
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let e"><span class="badge" [class]="e.status">{{ e.status }}</span></td>
        </ng-container>
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef></th>
          <td mat-cell *matCellDef="let e" class="actions">
            <button mat-icon-button matTooltip="Edit" (click)="openForm(e)"><mat-icon>edit</mat-icon></button>
            <button mat-icon-button matTooltip="Delete" (click)="remove(e)"><mat-icon>delete</mat-icon></button>
          </td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="columns"></tr>
        <tr mat-row *matRowDef="let row; columns: columns"></tr>
        <tr class="mat-mdc-row" *matNoDataRow>
          <td class="mat-mdc-cell empty" [attr.colspan]="columns.length">No employees match the filters.</td>
        </tr>
      </table>
      <mat-paginator [length]="total()" [pageIndex]="page()" [pageSize]="size()" [pageSizeOptions]="[10, 20, 50]"
                     (page)="onPage($event)" showFirstLastButtons />
    </div>
  `,
  styles: `
    .filters mat-form-field { width: 230px; }
    .filters .grow { flex: 1; min-width: 240px; }
    .filters .narrow { width: 160px; }
    .filters .loc { width: 260px; }
    .table-card { padding: 0; overflow: auto; margin-top: 8px; }
    .small { font: var(--mat-sys-body-small); }
    .actions { white-space: nowrap; text-align: right; }
    .empty { padding: 24px; text-align: center; color: var(--mat-sys-on-surface-variant); }
  `,
})
export class EmployeeList implements OnInit {
  private readonly api = inject(ApiService);
  private readonly lookups = inject(Lookups);
  private readonly dialog = inject(MatDialog);
  protected readonly indent = indent;

  /** Query params (?orgUnitId=2, ?locationId=7) so the organogram can link here. */
  readonly orgUnitId = input<string>();
  readonly locationId = input<string>();

  protected readonly columns = ['employeeCode', 'fullName', 'designationName', 'orgUnitName', 'locationName',
                                'supervisorName', 'status', 'actions'];
  protected readonly rows = signal<EmployeeView[]>([]);
  protected readonly total = signal(0);
  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly loading = signal(false);

  protected readonly designations = toSignal(this.api.designations(), { initialValue: [] });
  protected readonly orgUnits = toSignal(this.lookups.orgUnitOptions(), { initialValue: [] });

  private readonly fb = inject(FormBuilder);
  protected readonly filters = this.fb.group({
    q: [''],
    orgUnitId: [null as number | null],
    locationId: [null as number | null],
    designationId: [null as number | null],
    status: [null as RecordStatus | null],
  });

  constructor() {
    this.filters.valueChanges.pipe(debounceTime(300)).subscribe(() => {
      this.page.set(0);
      this.load();
    });
  }

  ngOnInit(): void {
    const org = Number(this.orgUnitId()) || null;
    const loc = Number(this.locationId()) || null;
    if (org || loc) {
      this.filters.patchValue({ orgUnitId: org, locationId: loc }, { emitEvent: false });
    }
    this.load();
  }

  protected load(): void {
    const f = this.filters.getRawValue();
    this.loading.set(true);
    this.api.employees({ ...f, page: this.page(), size: this.size() }).subscribe({
      next: p => {
        this.rows.set(p.content);
        this.total.set(p.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected onPage(e: PageEvent): void {
    this.page.set(e.pageIndex);
    this.size.set(e.pageSize);
    this.load();
  }

  protected openForm(e: EmployeeView | null): void {
    this.dialog.open(EmployeeFormDialog, { data: e, width: '720px', maxWidth: '95vw' })
      .afterClosed().subscribe(saved => { if (saved) this.load(); });
  }

  protected remove(e: EmployeeView): void {
    confirm(this.dialog, { title: 'Delete employee', message: `Delete ${e.employeeCode} · ${e.fullName}?` })
      .subscribe(ok => {
        if (ok) this.api.deleteEmployee(e.id).subscribe(() => this.load());
      });
  }
}
