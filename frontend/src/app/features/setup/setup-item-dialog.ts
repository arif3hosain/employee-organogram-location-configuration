import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Observable } from 'rxjs';
import { RecordStatus } from '../../core/models';

/**
 * Shape shared by every setup list. `value` is a number typed by the user (level, rank)
 * or the id picked from `options` (a designation's grade).
 */
export interface SetupItem {
  id: number;
  code: string;
  name: string;
  value: number;
  display: string;     // what the table shows for `value`
  status: RecordStatus;
  version: number;
}

export interface SetupItemValue {
  code: string;
  name: string;
  value: number;
  status: RecordStatus;
  version: number | null;
}

export interface SetupOption {
  id: number;
  label: string;
  disabled?: boolean;
}

export interface SetupItemDialogData {
  title: string;
  valueLabel: string;
  valueHint: string;
  /** When set, `value` is chosen from this list instead of typed. */
  options: SetupOption[] | null;
  item: SetupItem | null;
  save: (value: SetupItemValue) => Observable<unknown>;
}

@Component({
  selector: 'app-setup-item-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  template: `
    <h2 mat-dialog-title>{{ data.item ? 'Edit' : 'New' }} {{ data.title }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="form-grid" id="setupForm" (ngSubmit)="save()">
        <mat-form-field>
          <mat-label>Code</mat-label>
          <input matInput formControlName="code" maxlength="30" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Name</mat-label>
          <input matInput formControlName="name" maxlength="100" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>{{ data.valueLabel }}</mat-label>
          @if (data.options) {
            <mat-select formControlName="value">
              @for (o of data.options; track o.id) {
                <mat-option [value]="o.id" [disabled]="o.disabled && o.id !== data.item?.value">{{ o.label }}</mat-option>
              }
            </mat-select>
          } @else {
            <input matInput type="number" min="1" formControlName="value" />
          }
          <mat-hint>{{ data.valueHint }}</mat-hint>
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
      <button mat-flat-button type="submit" form="setupForm" [disabled]="form.invalid || saving()">Save</button>
    </mat-dialog-actions>
  `,
})
export class SetupItemDialog {
  protected readonly data = inject<SetupItemDialogData>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<SetupItemDialog, boolean>);
  protected readonly saving = signal(false);

  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly form = this.fb.group({
    code: [this.data.item?.code ?? '', Validators.required],
    name: [this.data.item?.name ?? '', Validators.required],
    value: this.fb.control<number | null>(this.data.item?.value ?? (this.data.options ? null : 1),
                                          [Validators.required, Validators.min(1)]),
    status: this.fb.control<RecordStatus>(this.data.item?.status ?? 'ACTIVE'),
  });

  protected save(): void {
    const v = this.form.getRawValue();
    this.saving.set(true);
    this.data.save({ ...v, value: v.value as number, version: this.data.item?.version ?? null }).subscribe({
      next: () => this.ref.close(true),
      error: () => this.saving.set(false),
    });
  }
}
