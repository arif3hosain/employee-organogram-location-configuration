import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ApiService } from '../../core/api.service';
import { OrgUnitForm, OrgUnitNature, OrgUnitNode, OrgUnitView, RecordStatus } from '../../core/models';
import { LocationPicker } from '../../shared/location-picker';
import { findNode, flattenTree, indent, subtreeIds } from '../../shared/tree-utils';

export interface OrgUnitDialogData {
  unit: OrgUnitView | null;   // null = create
  parentId: number | null;
  tree: OrgUnitNode[];
}

@Component({
  selector: 'app-org-unit-form-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule,
            LocationPicker],
  template: `
    <h2 mat-dialog-title>{{ data.unit ? 'Edit ' + data.unit.name : 'New org unit' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="form-grid" id="orgUnitForm" (ngSubmit)="save()">
        <mat-form-field>
          <mat-label>Code</mat-label>
          <input matInput formControlName="code" maxlength="30" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Name</mat-label>
          <input matInput formControlName="name" maxlength="150" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Type</mat-label>
          <mat-select formControlName="typeId">
            @for (t of types(); track t.id) {
              <mat-option [value]="t.id" [disabled]="t.status === 'INACTIVE'">L{{ t.levelNo }} · {{ t.name }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field>
          <mat-label>Nature</mat-label>
          <mat-select formControlName="nature">
            <mat-option [value]="null">Inherit from parent</mat-option>
            <mat-option value="FUNCTIONAL">FUNCTIONAL (core business)</mat-option>
            <mat-option value="NON_FUNCTIONAL">NON_FUNCTIONAL (support)</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field class="full">
          <mat-label>Parent unit</mat-label>
          <mat-select formControlName="parentId">
            <mat-option [value]="null">— none (top level) —</mat-option>
            @for (o of parentOptions(); track o.id) {
              <mat-option [value]="o.id">{{ indent(o.depth) }}{{ o.label }}</mat-option>
            }
          </mat-select>
          <mat-hint>Rule: the parent's type must be a higher level (smaller L number).</mat-hint>
        </mat-form-field>
        <app-location-picker class="full" formControlName="locationId" label="Location (optional)" />
        <mat-form-field>
          <mat-label>Sort order</mat-label>
          <input matInput type="number" formControlName="sortOrder" />
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
      <button mat-flat-button type="submit" form="orgUnitForm" [disabled]="form.invalid || saving()">Save</button>
    </mat-dialog-actions>
  `,
})
export class OrgUnitFormDialog {
  private readonly api = inject(ApiService);
  private readonly ref = inject(MatDialogRef<OrgUnitFormDialog, boolean>);
  protected readonly data = inject<OrgUnitDialogData>(MAT_DIALOG_DATA);
  protected readonly indent = indent;
  protected readonly saving = signal(false);
  protected readonly types = toSignal(this.api.orgUnitTypes(), { initialValue: [] });

  protected readonly parentOptions = computed(() => {
    const excluded = this.data.unit ? subtreeIds(findNode(this.data.tree, this.data.unit.id)) : new Set<number>();
    return flattenTree(this.data.tree, n => `${n.name} (${n.typeName})`).filter(o => !excluded.has(o.id));
  });

  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly form = this.fb.group({
    code: ['', Validators.required],
    name: ['', Validators.required],
    typeId: this.fb.control<number | null>(null, Validators.required),
    nature: this.fb.control<OrgUnitNature | null>(null),
    parentId: this.fb.control<number | null>(this.data.parentId),
    locationId: this.fb.control<number | null>(null),
    sortOrder: this.fb.control<number | null>(null),
    status: this.fb.control<RecordStatus>('ACTIVE'),
  });

  constructor() {
    const u = this.data.unit;
    if (u) {
      this.form.setValue({
        code: u.code, name: u.name, typeId: u.typeId, nature: u.nature, parentId: u.parentId,
        locationId: u.locationId, sortOrder: u.sortOrder, status: u.status,
      });
    }
  }

  protected save(): void {
    const body: OrgUnitForm = { ...this.form.getRawValue(), version: this.data.unit?.version ?? null };
    this.saving.set(true);
    const call = this.data.unit ? this.api.updateOrgUnit(this.data.unit.id, body) : this.api.createOrgUnit(body);
    call.subscribe({ next: () => this.ref.close(true), error: () => this.saving.set(false) });
  }
}
