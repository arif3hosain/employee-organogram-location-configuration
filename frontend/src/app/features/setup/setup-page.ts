import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Observable, forkJoin, map, of } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { CountryView, TypeView } from '../../core/models';
import { confirm } from '../../shared/confirm-dialog';
import { CountryDialog } from './country-dialog';
import { SetupItem, SetupItemDialog, SetupItemValue, SetupOption } from './setup-item-dialog';

type SetupKey = 'locationTypes' | 'orgUnitTypes' | 'grades' | 'designations';

/** Describes one setup list so the same table and dialog serve all of them. */
interface SetupKind {
  key: SetupKey;
  title: string;
  valueLabel: string;
  valueHint: string;
  load: () => Observable<SetupItem[]>;
  /** Pick-list for `value` (designation → grade); null = typed number. */
  options: () => Observable<SetupOption[] | null>;
  /** Location types only: each belongs to a country chosen in the dialog. */
  group?: { label: string; options: () => Observable<SetupOption[]> };
  create: (v: SetupItemValue) => Observable<unknown>;
  update: (id: number, v: SetupItemValue) => Observable<unknown>;
  remove: (id: number) => Observable<unknown>;
}

@Component({
  selector: 'app-setup-page',
  imports: [MatTabsModule, MatButtonModule, MatIconModule, MatTooltipModule],
  template: `
    <div class="page-header"><h1>Setup</h1></div>
    <mat-tab-group animationDuration="0ms">
      <mat-tab label="Countries">
        <div class="tab">
          <div class="row">
            <span class="muted">A country is its own record, not a location. Each country defines its own location levels
              (Location types tab), e.g. Bangladesh: Division › District › Upazila.</span>
            <span class="spacer"></span>
            <button mat-flat-button (click)="openCountry(null)"><mat-icon>add</mat-icon> New country</button>
          </div>
          <table class="setup">
            <thead><tr><th>ISO</th><th>Name</th><th>Currency</th><th>Phone</th><th>Status</th><th></th></tr></thead>
            <tbody>
              @for (c of countries(); track c.id) {
                <tr>
                  <td><strong>{{ c.iso2 }}</strong> <span class="muted">{{ c.iso3 }}</span></td>
                  <td>{{ c.name }}</td>
                  <td>{{ c.currencyCode }}</td>
                  <td>{{ c.phoneCode }}</td>
                  <td><span class="badge" [class]="c.status">{{ c.status }}</span></td>
                  <td class="actions">
                    <button mat-icon-button matTooltip="Edit" (click)="openCountry(c)"><mat-icon>edit</mat-icon></button>
                    <button mat-icon-button matTooltip="Delete" (click)="removeCountry(c)"><mat-icon>delete</mat-icon></button>
                  </td>
                </tr>
              } @empty {
                <tr><td colspan="6" class="muted">No countries yet.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </mat-tab>
      @for (k of kinds; track k.key) {
        <mat-tab [label]="k.title + 's'">
          <div class="tab">
            <div class="row">
              <span class="muted">{{ k.valueHint }}</span>
              <span class="spacer"></span>
              <button mat-flat-button (click)="open(k, null)"><mat-icon>add</mat-icon> New {{ k.title.toLowerCase() }}</button>
            </div>
            <table class="setup">
              <thead><tr>@if (k.group) { <th>{{ k.group.label }}</th> }<th>{{ k.valueLabel }}</th><th>Code</th><th>Name</th><th>Status</th><th></th></tr></thead>
              <tbody>
                @for (i of items()[k.key]; track i.id) {
                  <tr>
                    @if (k.group) { <td>{{ i.groupDisplay }}</td> }
                    <td class="val">{{ i.display }}</td>
                    <td><strong>{{ i.code }}</strong></td>
                    <td>{{ i.name }}</td>
                    <td><span class="badge" [class]="i.status">{{ i.status }}</span></td>
                    <td class="actions">
                      <button mat-icon-button matTooltip="Edit" (click)="open(k, i)"><mat-icon>edit</mat-icon></button>
                      <button mat-icon-button matTooltip="Delete" (click)="remove(k, i)"><mat-icon>delete</mat-icon></button>
                    </td>
                  </tr>
                } @empty {
                  <tr><td [attr.colspan]="k.group ? 6 : 5" class="muted">Nothing defined yet.</td></tr>
                }
              </tbody>
            </table>
          </div>
        </mat-tab>
      }
    </mat-tab-group>
  `,
  styles: `
    .tab { padding: 16px 0; max-width: 900px; }
    table.setup { width: 100%; border-collapse: collapse; margin-top: 12px; }
    .setup th, .setup td { padding: 6px 8px; border-bottom: 1px solid var(--mat-sys-outline-variant); text-align: left; }
    .setup th { font: var(--mat-sys-label-medium); color: var(--mat-sys-on-surface-variant); }
    .val { width: 220px; }
    .actions { text-align: right; white-space: nowrap; }
  `,
})
export class SetupPage {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);

  protected readonly countries = signal<CountryView[]>([]);
  protected readonly items = signal<Record<SetupKey, SetupItem[]>>({
    locationTypes: [], orgUnitTypes: [], grades: [], designations: [],
  });

  protected readonly kinds: SetupKind[] = [
    {
      key: 'locationTypes', title: 'Location type', valueLabel: 'Level',
      valueHint: 'Per country. Level 1 = top (e.g. Division in Bangladesh, State in India). A location may only sit under a higher-level location of the same country.',
      load: () => this.api.locationTypes().pipe(map(ts => ts.map(t => ({
        ...t, value: t.levelNo, display: String(t.levelNo), group: t.countryId ?? null, groupDisplay: t.countryName ?? null,
      })))),
      options: () => of(null),
      group: {
        label: 'Country',
        options: () => this.api.countries().pipe(map(cs => cs.map(c => ({
          id: c.id, label: `${c.name} (${c.iso2})`, disabled: c.status === 'INACTIVE',
        })))),
      },
      create: v => this.api.createLocationType(this.toType(v)),
      update: (id, v) => this.api.updateLocationType(id, this.toType(v)),
      remove: id => this.api.deleteLocationType(id),
    },
    {
      key: 'orgUnitTypes', title: 'Org unit type', valueLabel: 'Level',
      valueHint: 'Level 1 = top (Organization), then Function, Department, Unit, Team…',
      load: () => this.typeItems(this.api.orgUnitTypes()),
      options: () => of(null),
      create: v => this.api.createOrgUnitType(this.toType(v)),
      update: (id, v) => this.api.updateOrgUnitType(id, this.toType(v)),
      remove: id => this.api.deleteOrgUnitType(id),
    },
    {
      key: 'grades', title: 'Grade', valueLabel: 'Rank',
      valueHint: 'Rank 1 = most senior. Each grade has its own rank; designations are linked to a grade.',
      load: () => this.api.grades().pipe(map(gs => gs.map(g => ({ ...g, value: g.rankNo, display: String(g.rankNo) })))),
      options: () => of(null),
      create: v => this.api.createGrade(this.toGrade(v)),
      update: (id, v) => this.api.updateGrade(id, this.toGrade(v)),
      remove: id => this.api.deleteGrade(id),
    },
    {
      key: 'designations', title: 'Designation', valueLabel: 'Grade',
      valueHint: 'Each designation belongs to a grade (maintain grades in the Grades tab).',
      load: () => this.api.designations().pipe(map(ds => ds.map(d => ({
        ...d, value: d.gradeId, display: `${d.gradeCode} · ${d.gradeName}`,
      })))),
      options: () => this.api.grades().pipe(map(gs => gs.map(g => ({
        id: g.id, label: `${g.code} · ${g.name}`, disabled: g.status === 'INACTIVE',
      })))),
      create: v => this.api.createDesignation(this.toDesignation(v)),
      update: (id, v) => this.api.updateDesignation(id, this.toDesignation(v)),
      remove: id => this.api.deleteDesignation(id),
    },
  ];

  constructor() {
    this.reloadCountries();
    this.kinds.forEach(k => this.reload(k));
  }

  private reloadCountries(): void {
    this.api.countries().subscribe(cs => this.countries.set(cs));
  }

  protected openCountry(c: CountryView | null): void {
    this.dialog.open(CountryDialog, { width: '600px', data: c }).afterClosed().subscribe(saved => {
      if (!saved) return;
      this.reloadCountries();
      this.reload(this.kinds[0]);       // country names show on location types
    });
  }

  protected removeCountry(c: CountryView): void {
    confirm(this.dialog, { title: 'Delete country', message: `Delete ${c.name}? This fails if it has location types.` })
      .subscribe(ok => { if (ok) this.api.deleteCountry(c.id).subscribe(() => this.reloadCountries()); });
  }

  private reload(k: SetupKind): void {
    k.load().subscribe(list => this.items.update(all => ({ ...all, [k.key]: list })));
  }

  protected open(k: SetupKind, item: SetupItem | null): void {
    forkJoin([k.options(), k.group ? k.group.options() : of(null)]).subscribe(([options, groupOptions]) => {
      this.dialog.open(SetupItemDialog, {
        width: '600px',
        data: {
          title: k.title.toLowerCase(), valueLabel: k.valueLabel, valueHint: k.valueHint, options, item,
          groupLabel: k.group?.label, groupOptions,
          save: (v: SetupItemValue) => (item ? k.update(item.id, v) : k.create(v)),
        },
      }).afterClosed().subscribe(saved => {
        if (!saved) return;
        this.reload(k);
        if (k.key === 'grades') this.reload(this.kinds.find(x => x.key === 'designations')!);  // grade names show there
      });
    });
  }

  protected remove(k: SetupKind, item: SetupItem): void {
    confirm(this.dialog, { title: `Delete ${k.title.toLowerCase()}`, message: `Delete ${item.code} · ${item.name}?` })
      .subscribe(ok => { if (ok) k.remove(item.id).subscribe(() => this.reload(k)); });
  }

  private typeItems(src: Observable<TypeView[]>): Observable<SetupItem[]> {
    return src.pipe(map(ts => ts.map(t => ({ ...t, value: t.levelNo, display: String(t.levelNo) }))));
  }

  private toType(v: SetupItemValue) {
    const country = v.group === null ? {} : { countryId: v.group };   // location types only
    return { ...country, code: v.code, name: v.name, levelNo: v.value, status: v.status, version: v.version };
  }

  private toGrade(v: SetupItemValue) {
    return { code: v.code, name: v.name, rankNo: v.value, status: v.status, version: v.version };
  }

  private toDesignation(v: SetupItemValue) {
    return { code: v.code, name: v.name, gradeId: v.value, status: v.status, version: v.version };
  }
}
