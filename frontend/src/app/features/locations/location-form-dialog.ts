import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ApiService } from '../../core/api.service';
import { LocationForm, LocationNode, LocationView, RecordStatus, TypeView } from '../../core/models';
import { findNode, flattenTree, indent, isCountryHeading, subtreeIds } from '../../shared/tree-utils';

export interface LocationDialogData {
  location: LocationView | null;   // null = create
  parentId: number | null;         // pre-selected parent when adding a child
  tree: LocationNode[];
}

@Component({
  selector: 'app-location-form-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  template: `
    <h2 mat-dialog-title>{{ data.location ? 'Edit ' + data.location.name : 'New location' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="form-grid" id="locationForm" (ngSubmit)="save()">
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
            @for (g of typeGroups(); track g.country) {
              <mat-optgroup [label]="g.country">
                @for (t of g.types; track t.id) {
                  <mat-option [value]="t.id" [disabled]="t.status === 'INACTIVE'">L{{ t.levelNo }} · {{ t.name }}</mat-option>
                }
              </mat-optgroup>
            }
          </mat-select>
          <mat-hint>The type decides the country.</mat-hint>
        </mat-form-field>
        <mat-form-field>
          <mat-label>Status</mat-label>
          <mat-select formControlName="status">
            <mat-option value="ACTIVE">ACTIVE</mat-option>
            <mat-option value="INACTIVE">INACTIVE</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field class="full">
          <mat-label>Parent location</mat-label>
          <mat-select formControlName="parentId">
            <mat-option [value]="null">— none (top level) —</mat-option>
            @for (o of parentOptions(); track o.id) {
              <mat-option [value]="o.id">{{ indent(o.depth) }}{{ o.label }}</mat-option>
            }
          </mat-select>
          <mat-hint>Same country as the type, and a higher level (smaller L number). Levels may be skipped.</mat-hint>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-flat-button type="submit" form="locationForm" [disabled]="form.invalid || saving()">Save</button>
    </mat-dialog-actions>
  `,
})
export class LocationFormDialog {
  private readonly api = inject(ApiService);
  private readonly ref = inject(MatDialogRef<LocationFormDialog, boolean>);
  protected readonly data = inject<LocationDialogData>(MAT_DIALOG_DATA);
  protected readonly indent = indent;
  protected readonly saving = signal(false);
  protected readonly types = toSignal(this.api.locationTypes(), { initialValue: [] });

  /** Types grouped by country for the drop-down. */
  protected readonly typeGroups = computed(() => {
    const groups = new Map<string, TypeView[]>();
    this.types().forEach(t => groups.set(t.countryName ?? '', [...(groups.get(t.countryName ?? '') ?? []), t]));
    return [...groups].map(([country, types]) => ({ country, types }));
  });

  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly form = this.fb.group({
    code: ['', Validators.required],
    name: ['', Validators.required],
    typeId: this.fb.control<number | null>(null, Validators.required),
    parentId: this.fb.control<number | null>(this.data.parentId),
    status: this.fb.control<RecordStatus>('ACTIVE'),
  });

  private readonly typeId = toSignal(this.form.controls.typeId.valueChanges, { initialValue: this.form.controls.typeId.value });

  /**
   * Every location except the one being edited and its descendants (no cycles), limited to the
   * chosen type's country. Country headings in the tree are not locations and are skipped.
   */
  protected readonly parentOptions = computed(() => {
    const excluded = this.data.location ? subtreeIds(findNode(this.data.tree, this.data.location.id)) : new Set<number>();
    const countryId = this.types().find(t => t.id === this.typeId())?.countryId;
    const roots = this.data.tree.flatMap(n => (isCountryHeading(n.id) ? n.children : [n]))
      .filter(n => countryId === undefined || n.countryId === countryId);
    return flattenTree(roots, n => `${n.name} (${n.typeName})`).filter(o => !excluded.has(o.id));
  });

  constructor() {
    const l = this.data.location;
    if (l) {
      this.form.setValue({ code: l.code, name: l.name, typeId: l.typeId, parentId: l.parentId, status: l.status });
    }
  }

  protected save(): void {
    const body: LocationForm = { ...this.form.getRawValue(), version: this.data.location?.version ?? null };
    this.saving.set(true);
    const call = this.data.location ? this.api.updateLocation(this.data.location.id, body) : this.api.createLocation(body);
    call.subscribe({ next: () => this.ref.close(true), error: () => this.saving.set(false) });
  }
}
