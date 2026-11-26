import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ApiService } from '../../core/api.service';
import { CountryForm, CountryView, RecordStatus } from '../../core/models';

/** Create / edit a country. Its location levels are maintained in the Location types tab. */
@Component({
  selector: 'app-country-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  template: `
    <h2 mat-dialog-title>{{ country ? 'Edit ' + country.name : 'New country' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="form-grid" id="countryForm" (ngSubmit)="save()">
        <mat-form-field class="full">
          <mat-label>Name</mat-label>
          <input matInput formControlName="name" maxlength="100" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>ISO code (2 letters)</mat-label>
          <input matInput formControlName="iso2" maxlength="2" placeholder="BD" />
          <mat-hint>ISO 3166-1 alpha-2</mat-hint>
        </mat-form-field>
        <mat-form-field>
          <mat-label>ISO code (3 letters)</mat-label>
          <input matInput formControlName="iso3" maxlength="3" placeholder="BGD" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Currency</mat-label>
          <input matInput formControlName="currencyCode" maxlength="3" placeholder="BDT" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Phone code</mat-label>
          <input matInput formControlName="phoneCode" maxlength="5" placeholder="+880" />
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
      <button mat-flat-button type="submit" form="countryForm" [disabled]="form.invalid || saving()">Save</button>
    </mat-dialog-actions>
  `,
})
export class CountryDialog {
  private readonly api = inject(ApiService);
  private readonly ref = inject(MatDialogRef<CountryDialog, boolean>);
  protected readonly country = inject<CountryView | null>(MAT_DIALOG_DATA);
  protected readonly saving = signal(false);

  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly form = this.fb.group({
    name: [this.country?.name ?? '', Validators.required],
    iso2: [this.country?.iso2 ?? '', [Validators.required, Validators.pattern(/^[A-Za-z]{2}$/)]],
    iso3: [this.country?.iso3 ?? '', Validators.pattern(/^[A-Za-z]{3}$/)],
    currencyCode: [this.country?.currencyCode ?? '', Validators.pattern(/^[A-Za-z]{3}$/)],
    phoneCode: [this.country?.phoneCode ?? '', Validators.pattern(/^\+[0-9]{1,4}$/)],
    status: this.fb.control<RecordStatus>(this.country?.status ?? 'ACTIVE'),
  });

  protected save(): void {
    const v = this.form.getRawValue();
    const body: CountryForm = {
      ...v,
      iso3: v.iso3 || null, currencyCode: v.currencyCode || null, phoneCode: v.phoneCode || null,
      version: this.country?.version ?? null,
    };
    this.saving.set(true);
    const call = this.country ? this.api.updateCountry(this.country.id, body) : this.api.createCountry(body);
    call.subscribe({ next: () => this.ref.close(true), error: () => this.saving.set(false) });
  }
}
