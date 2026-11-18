import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from './api.service';
import { TreeOption, flattenTree } from '../shared/tree-utils';

/** Dropdown data built from the tree endpoints (indented by level). Not cached: setup can change. */
@Injectable({ providedIn: 'root' })
export class Lookups {
  private readonly api = inject(ApiService);

  orgUnitOptions(): Observable<TreeOption[]> {
    return this.api.orgUnitTree().pipe(map(t => flattenTree(t, n => `${n.name} (${n.typeName})`)));
  }

  locationOptions(): Observable<TreeOption[]> {
    return this.api.locationTree().pipe(map(t => flattenTree(t, n => `${n.name} (${n.typeName})`)));
  }
}
