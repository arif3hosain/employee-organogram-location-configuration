import { Component, computed, ElementRef, forwardRef, inject, input, signal, viewChild } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { CdkConnectedOverlay, CdkOverlayOrigin } from '@angular/cdk/overlay';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ApiService } from '../core/api.service';
import { LocationNode } from '../core/models';
import { TreeView } from './tree-view';
import { ancestorIds, findNode } from './tree-utils';

/**
 * Drop-down tree for picking a location. The field opens a panel with the whole
 * location tree (Bangladesh › Division › District › Upazila / Office). Nodes expand and
 * collapse, a search box narrows the tree, and clicking any node at any level picks it.
 *
 * Form control value = the picked location id (or null).
 *   <app-location-picker formControlName="locationId" label="Workplace" [required]="true" />
 *   <app-location-picker formControlName="locationId" variant="filter" />   (list filter: "All locations")
 */
@Component({
  selector: 'app-location-picker',
  imports: [CdkOverlayOrigin, CdkConnectedOverlay, TreeView, MatFormFieldModule, MatInputModule, MatIconModule, MatButtonModule],
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => LocationPicker), multi: true }],
  template: `
    <mat-form-field class="field" cdkOverlayOrigin #origin="cdkOverlayOrigin">
      <mat-label>{{ label() }}</mat-label>
      <mat-icon matPrefix>location_on</mat-icon>
      <input matInput readonly [required]="required()" [disabled]="disabled()"
             [value]="selectedNode()?.name ?? (variant() === 'filter' ? 'All locations' : '')"
             (click)="toggle()" (keydown.enter)="toggle()" (keydown.arrowdown)="openPanel()"
             [placeholder]="variant() === 'filter' ? 'All locations' : 'Click to choose'" />
      <span matSuffix class="suffix">
        @if (selectedNode() && !disabled()) {
          <button type="button" class="clear" aria-label="Clear" (click)="clear(); $event.stopPropagation()">
            <mat-icon>close</mat-icon>
          </button>
        }
        <mat-icon class="arrow" (click)="toggle()">{{ open() ? 'arrow_drop_up' : 'arrow_drop_down' }}</mat-icon>
      </span>
      @if (variant() === 'form' && pathLabel()) {
        <mat-hint>{{ pathLabel() }}</mat-hint>
      }
    </mat-form-field>

    <ng-template cdkConnectedOverlay [cdkConnectedOverlayOrigin]="origin" [cdkConnectedOverlayOpen]="open()"
                 [cdkConnectedOverlayHasBackdrop]="true" cdkConnectedOverlayBackdropClass="cdk-overlay-transparent-backdrop"
                 [cdkConnectedOverlayMinWidth]="panelWidth()" (backdropClick)="close()" (detach)="close()"
                 (overlayKeydown)="$event.key === 'Escape' && close()">
      <div class="tree-panel mat-elevation-z8" role="dialog" aria-label="Choose a location">
        <div class="search">
          <mat-icon class="muted">search</mat-icon>
          <input #search placeholder="Search location (e.g. Mirpur, Savar, Sylhet)" [value]="query()"
                 (input)="setQuery($any($event.target).value)" />
          @if (query()) {
            <button mat-icon-button type="button" aria-label="Clear search" (click)="setQuery('')"><mat-icon>close</mat-icon></button>
          }
        </div>
        @if (variant() === 'filter') {
          <div class="all" [class.selected]="value() === null" (click)="choose(null)">All locations</div>
        }
        <div class="tree-scroll">
          <ng-template #nodeLabel let-node>
            <span class="name" [innerHTML]="highlight(node.name)"></span>
            <span class="type">{{ node.typeName }}</span>
          </ng-template>
          @if (visibleTree().length) {
            <app-tree-view [nodes]="visibleTree()" [nodeTemplate]="nodeLabel" [selectedId]="value()"
                           [(expanded)]="expanded" (picked)="choose($event.id)" />
          } @else {
            <p class="muted empty">{{ tree().length ? 'No location matches "' + query() + '"' : 'Loading…' }}</p>
          }
        </div>
      </div>
    </ng-template>
  `,
  styles: `
    :host { display: block; }
    .field { width: 100%; }
    input[readonly] { cursor: pointer; }
    .suffix { display: inline-flex; align-items: center; gap: 2px; padding-right: 8px; }
    .clear { border: 0; background: none; padding: 0; width: 24px; height: 24px; cursor: pointer;
             display: inline-flex; align-items: center; justify-content: center; color: var(--mat-sys-on-surface-variant);
             mat-icon { font-size: 18px; width: 18px; height: 18px; } }
    .arrow { cursor: pointer; color: var(--mat-sys-on-surface-variant); }
    .tree-panel {
      background: var(--mat-sys-surface-container-lowest); border-radius: 8px; margin-top: 2px;
      border: 1px solid var(--mat-sys-outline-variant); box-shadow: 0 6px 20px rgb(0 0 0 / .18);
      display: flex; flex-direction: column; max-height: 440px; min-width: 320px;
    }
    .search {
      display: flex; align-items: center; gap: 6px; padding: 6px 8px;
      border-bottom: 1px solid var(--mat-sys-outline-variant);
      input { flex: 1; border: 0; outline: 0; font: inherit; padding: 8px 0; background: transparent; color: inherit; }
    }
    .all {
      padding: 8px 14px; cursor: pointer; font-weight: 500; border-bottom: 1px solid var(--mat-sys-outline-variant);
      &:hover { background: var(--mat-sys-surface-container-high); }
      &.selected { background: var(--mat-sys-secondary-container); }
    }
    .tree-scroll { overflow: auto; padding: 6px; }
    .empty { padding: 8px; }
    .name { white-space: nowrap; }
    .type { font: var(--mat-sys-label-small); color: var(--mat-sys-on-surface-variant); margin-left: 6px; }
  `,
})
export class LocationPicker implements ControlValueAccessor {
  private readonly api = inject(ApiService);
  private readonly host = inject(ElementRef<HTMLElement>);
  private readonly searchInput = viewChild<ElementRef<HTMLInputElement>>('search');

  readonly label = input('Location');
  readonly required = input(false);
  /** 'form' for edit forms (shows the full path as a hint), 'filter' for list filters ("All locations"). */
  readonly variant = input<'form' | 'filter'>('form');

  protected readonly tree = signal<LocationNode[]>([]);
  protected readonly value = signal<number | null>(null);
  protected readonly open = signal(false);
  protected readonly query = signal('');
  protected readonly expanded = signal<Set<number>>(new Set());
  protected readonly disabled = signal(false);
  protected readonly panelWidth = signal(360);

  private onChange: (v: number | null) => void = () => {};
  private onTouched: () => void = () => {};

  protected readonly selectedNode = computed(() => {
    const id = this.value();
    return id === null ? null : findNode(this.tree(), id);
  });

  /** "Bangladesh › Dhaka › Dhaka › Mirpur-10 Branch" */
  protected readonly pathLabel = computed(() => {
    const id = this.value();
    if (id === null) return '';
    const trail = ancestorIds(this.tree(), id) ?? [];
    return [...trail, id].map(x => findNode(this.tree(), x)?.name ?? '?').join(' › ');
  });

  /** The tree, or only the matching nodes and their ancestors while searching. Sorted by name per level. */
  protected readonly visibleTree = computed(() => {
    const q = this.query().trim().toLowerCase();
    return q ? filterTree(this.tree(), q) : sortTree(this.tree());
  });

  constructor() {
    this.api.locationTree().subscribe(t => this.tree.set(t));
  }

  protected toggle(): void {
    if (this.open()) this.close(); else this.openPanel();
  }

  protected openPanel(): void {
    if (this.disabled()) return;
    this.panelWidth.set(Math.max(360, this.host.nativeElement.getBoundingClientRect().width));
    this.query.set('');
    // show the top two levels, plus the way down to the current choice
    const open = new Set<number>(this.tree().map(n => n.id));
    const id = this.value();
    if (id !== null) (ancestorIds(this.tree(), id) ?? []).forEach(a => open.add(a));
    this.expanded.set(open);
    this.open.set(true);
    setTimeout(() => this.searchInput()?.nativeElement.focus());
  }

  protected close(): void {
    if (!this.open()) return;
    this.open.set(false);
    this.onTouched();
  }

  protected setQuery(q: string): void {
    this.query.set(q);
    if (q.trim()) {
      // expand everything that is left, so every match is visible
      const ids = new Set<number>();
      const walk = (ns: LocationNode[]) => ns.forEach(n => { if (n.children.length) ids.add(n.id); walk(n.children); });
      walk(this.visibleTree());
      this.expanded.set(ids);
    }
  }

  protected choose(id: number | null): void {
    this.value.set(id);
    this.onChange(id);
    this.close();
  }

  protected clear(): void {
    this.choose(null);
  }

  /** Bold the matched part of a name while searching. */
  protected highlight(name: string): string {
    const safe = escapeHtml(name);
    const q = this.query().trim();
    if (!q) return safe;
    const i = name.toLowerCase().indexOf(q.toLowerCase());
    if (i < 0) return safe;
    return escapeHtml(name.slice(0, i)) + '<b>' + escapeHtml(name.slice(i, i + q.length)) + '</b>' + escapeHtml(name.slice(i + q.length));
  }

  /* ---------------- ControlValueAccessor ---------------- */

  writeValue(id: number | null): void {
    this.value.set(id ?? null);
  }

  registerOnChange(fn: (v: number | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled.set(isDisabled);
  }
}

function sortTree(nodes: LocationNode[]): LocationNode[] {
  return [...nodes]
    .sort((a, b) => a.name.localeCompare(b.name))
    .map(n => ({ ...n, children: sortTree(n.children) }));
}

/** Nodes whose name matches, plus their ancestors (so matches stay in context). */
function filterTree(nodes: LocationNode[], q: string): LocationNode[] {
  const out: LocationNode[] = [];
  for (const n of [...nodes].sort((a, b) => a.name.localeCompare(b.name))) {
    const children = filterTree(n.children, q);
    if (n.name.toLowerCase().includes(q) || n.code.toLowerCase() === q || children.length) {
      out.push({ ...n, children });
    }
  }
  return out;
}

function escapeHtml(s: string): string {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
