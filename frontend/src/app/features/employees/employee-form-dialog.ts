import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { debounceTime, distinctUntilChanged, filter, map, of, switchMap } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Lookups } from '../../core/lookups';
import { EmployeeForm, EmployeeView, RecordStatus } from '../../core/models';
import { LocationPicker } from '../../shared/location-picker';
import { indent } from '../../shared/tree-utils';

/**
 * Create / edit an employee. Only basic checks run here (required, email);
 * the real rules (duplicates, reporting loop, stale version) come back from the
 * backend (Bean Validation / service rules) and are shown as messages.
 */
@Component({
  selector: 'app-employee-form-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule,
            MatSelectModule, MatAutocompleteModule, LocationPicker],
  template: `
    <h2 mat-dialog-title>{{ editing ? 'Edit ' + editing.employeeCode : 'New employee' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="form-grid" id="employeeForm" (ngSubmit)="save()">
        <mat-form-field>
          <mat-label>Employee code</mat-label>
          <input matInput formControlName="employeeCode" maxlength="30" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Full name</mat-label>
          <input matInput formControlName="fullName" maxlength="150" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Email</mat-label>
          <input matInput formControlName="email" type="email" />
          @if (form.controls.email.hasError('email')) { <mat-error>Not a valid email</mat-error> }
        </mat-form-field>
        <mat-form-field>
          <mat-label>Phone</mat-label>
          <input matInput formControlName="phone" placeholder="+8801..." />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Join date</mat-label>
          <input matInput formControlName="joinDate" type="date" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Designation</mat-label>
          <mat-select formControlName="designationId">
            @for (d of designations(); track d.id) {
              <mat-option [value]="d.id" [disabled]="d.status === 'INACTIVE'">{{ d.name }} <span class="muted">· {{ d.gradeCode }}</span></mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field class="full">
          <mat-label>Org unit (organogram)</mat-label>
          <mat-select formControlName="orgUnitId">
            @for (o of orgUnits(); track o.id) {
              <mat-option [value]="o.id">{{ indent(o.depth) }}{{ o.label }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <app-location-picker class="full" formControlName="locationId" label="Workplace" [required]="true" />
        <mat-form-field>
          <mat-label>Supervisor</mat-label>
          <input matInput [formControl]="supervisorSearch" [matAutocomplete]="auto" placeholder="Type a name or code" />
          <mat-autocomplete #auto="matAutocomplete" [displayWith]="displaySupervisor" (optionSelected)="supervisorPicked($event.option.value)">
            <mat-option [value]="null"><em>No supervisor</em></mat-option>
            @for (e of supervisorOptions(); track e.id) {
              <mat-option [value]="e">{{ e.employeeCode }} · {{ e.fullName }} <span class="muted">({{ e.designationName }})</span></mat-option>
            }
          </mat-autocomplete>
        </mat-form-field>
        <mat-form-field>
          <mat-label>Status</mat-label>
          <mat-select formControlName="status">
            <mat-option value="ACTIVE">ACTIVE</mat-option>
            <mat-option value="INACTIVE">INACTIVE</mat-option>
          </mat-select>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-flat-button type="submit" form="employeeForm" [disabled]="form.invalid || saving()">
        {{ saving() ? 'Saving…' : 'Save' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class EmployeeFormDialog {
  private readonly api = inject(ApiService);
  private readonly lookups = inject(Lookups);
  private readonly ref = inject(MatDialogRef<EmployeeFormDialog, boolean>);
  protected readonly editing = inject<EmployeeView | null>(MAT_DIALOG_DATA);
  protected readonly indent = indent;

  protected readonly designations = toSignal(this.api.designations(), { initialValue: [] });
  protected readonly orgUnits = toSignal(this.lookups.orgUnitOptions(), { initialValue: [] });
  protected readonly saving = signal(false);

  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly form = this.fb.group({
    employeeCode: ['', Validators.required],
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phone: [''],
    joinDate: [new Date().toISOString().slice(0, 10), Validators.required],
    designationId: this.fb.control<number | null>(null, Validators.required),
    orgUnitId: this.fb.control<number | null>(null, Validators.required),
    locationId: this.fb.control<number | null>(null, Validators.required),
    status: this.fb.control<RecordStatus>('ACTIVE'),
  });

  /** Text typed in the supervisor box, or the picked employee. */
  protected readonly supervisorSearch = this.fb.control<string | EmployeeView | null>(null);
  private supervisorId: number | null = null;

  protected readonly supervisorOptions = toSignal(
    this.supervisorSearch.valueChanges.pipe(
      filter((v): v is string => typeof v === 'string'),
      debounceTime(250),
      distinctUntilChanged(),
      switchMap(q => (q.trim().length < 1
        ? of([] as EmployeeView[])
        : this.api.employees({ q, status: 'ACTIVE', page: 0, size: 10 }).pipe(
            map(p => p.content.filter(e => e.id !== this.editing?.id))))),   // can't report to yourself
    ),
    { initialValue: [] as EmployeeView[] },
  );

  constructor() {
    const e = this.editing;
    if (e) {
      this.form.setValue({
        employeeCode: e.employeeCode, fullName: e.fullName, email: e.email, phone: e.phone ?? '',
        joinDate: e.joinDate, designationId: e.designationId, orgUnitId: e.orgUnitId,
        locationId: e.locationId, status: e.status,
      });
      this.supervisorId = e.supervisorId;
      this.supervisorSearch.setValue(e.supervisorName ?? '', { emitEvent: false });
    }
  }

  protected readonly displaySupervisor = (v: string | EmployeeView | null): string =>
    v === null ? '' : typeof v === 'string' ? v : `${v.employeeCode} · ${v.fullName}`;

  protected supervisorPicked(e: EmployeeView | null): void {
    this.supervisorId = e?.id ?? null;
    if (!e) this.supervisorSearch.setValue('', { emitEvent: false });
  }

  protected save(): void {
    if (this.form.invalid) return;
    // A cleared text box means "no supervisor".
    if (this.supervisorSearch.value === '' || this.supervisorSearch.value === null) this.supervisorId = null;
    const v = this.form.getRawValue();
    const body: EmployeeForm = {
      ...v,
      phone: v.phone || null,
      supervisorId: this.supervisorId,
      version: this.editing?.version ?? null,
    };
    this.saving.set(true);
    const call = this.editing ? this.api.updateEmployee(this.editing.id, body) : this.api.createEmployee(body);
    call.subscribe({
      next: () => this.ref.close(true),
      error: () => this.saving.set(false),   // message already shown; keep the dialog open to fix it
    });
  }
}
