import { Component, ViewEncapsulation, input, model } from '@angular/core';
import { OrgUnitNode } from '../../core/models';

/** Top-down organogram chart (boxes and connector lines), drawn with nested lists and CSS. */
@Component({
  selector: 'app-org-chart',
  encapsulation: ViewEncapsulation.None,
  template: `
    <ul class="oc-level">
      @for (n of nodes(); track n.id) {
        <li>
          <button type="button" class="oc-box" [class]="'oc-box ' + (n.nature ?? 'NONE')"
                  [class.selected]="selectedId() === n.id" [class.inactive]="n.status === 'INACTIVE'"
                  (click)="selectedId.set(n.id)">
            <span class="oc-type">{{ n.typeName }}</span>
            <span class="oc-name">{{ n.name }}</span>
            <span class="oc-meta">{{ n.code }}@if (n.locationName) { · {{ n.locationName }} }</span>
          </button>
          @if (n.children.length) {
            <app-org-chart [nodes]="n.children" [(selectedId)]="selectedId" />
          }
        </li>
      }
    </ul>
  `,
  styles: `
    app-org-chart { display: block; }
    .oc-level { display: flex; justify-content: center; padding: 20px 0 0; margin: 0; position: relative; }
    .oc-level li { list-style: none; text-align: center; position: relative; padding: 20px 6px 0; }
    /* horizontal + vertical connectors */
    .oc-level li::before, .oc-level li::after {
      content: ''; position: absolute; top: 0; right: 50%; width: 50%; height: 20px;
      border-top: 1.5px solid var(--mat-sys-outline-variant);
    }
    .oc-level li::after { right: auto; left: 50%; border-left: 1.5px solid var(--mat-sys-outline-variant); }
    .oc-level li:only-child::before, .oc-level li:only-child::after { display: none; }
    .oc-level li:only-child { padding-top: 0; }
    .oc-level li:first-child::before, .oc-level li:last-child::after { border: 0 none; }
    .oc-level li:last-child::before { border-right: 1.5px solid var(--mat-sys-outline-variant); border-radius: 0 6px 0 0; }
    .oc-level li:first-child::after { border-radius: 6px 0 0 0; }
    .oc-level .oc-level::before {
      content: ''; position: absolute; top: 0; left: 50%; height: 20px;
      border-left: 1.5px solid var(--mat-sys-outline-variant);
    }
    app-org-chart:first-child > .oc-level { padding-top: 0; }

    .oc-box {
      display: inline-flex; flex-direction: column; gap: 2px; min-width: 150px; max-width: 200px;
      padding: 8px 12px; border-radius: 10px; cursor: pointer; font: inherit; text-align: center;
      background: var(--mat-sys-surface-container-lowest);
      border: 1px solid var(--mat-sys-outline-variant); border-top: 4px solid var(--mat-sys-outline);
      color: var(--mat-sys-on-surface);
      transition: box-shadow .15s, transform .15s;
      &:hover { box-shadow: 0 2px 8px rgb(0 0 0 / .12); transform: translateY(-1px); }
      &.FUNCTIONAL { border-top-color: #2f6fdb; }
      &.NON_FUNCTIONAL { border-top-color: #d99100; }
      &.selected { outline: 2px solid var(--mat-sys-primary); outline-offset: 2px; }
      &.inactive { opacity: .55; border-style: dashed; }
    }
    .oc-type { font: var(--mat-sys-label-small); text-transform: uppercase; letter-spacing: .5px; color: var(--mat-sys-on-surface-variant); }
    .oc-name { font-weight: 500; }
    .oc-meta { font: var(--mat-sys-body-small); color: var(--mat-sys-on-surface-variant); }
  `,
})
export class OrgChart {
  readonly nodes = input.required<OrgUnitNode[]>();
  readonly selectedId = model<number | null>(null);
}
