import { Component, TemplateRef, input, model, output } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { TreeNodeLike } from './tree-utils';

/**
 * Small recursive tree used for locations and the organogram's tree view.
 * The caller supplies the node label through an <ng-template let-node>.
 */
@Component({
  selector: 'app-tree-view',
  imports: [NgTemplateOutlet, MatIconModule],
  template: `
    <ul class="tree" [class.root]="depth() === 0">
      @for (node of nodes(); track node.id) {
        <li>
          <div class="node" [class.selected]="selectedId() === node.id" (click)="select(node)">
            @if (node.children.length) {
              <button type="button" class="toggle" (click)="toggle(node.id); $event.stopPropagation()"
                      [attr.aria-label]="isOpen(node.id) ? 'Collapse' : 'Expand'">
                <mat-icon>{{ isOpen(node.id) ? 'expand_more' : 'chevron_right' }}</mat-icon>
              </button>
            } @else {
              <span class="toggle leaf"></span>
            }
            <ng-container *ngTemplateOutlet="nodeTemplate(); context: { $implicit: node }" />
          </div>
          @if (node.children.length && isOpen(node.id)) {
            <app-tree-view [nodes]="node.children" [nodeTemplate]="nodeTemplate()" [depth]="depth() + 1"
                           [(selectedId)]="selectedId" [(expanded)]="expanded" (picked)="picked.emit($event)" />
          }
        </li>
      }
    </ul>
  `,
  styles: `
    ul.tree { list-style: none; margin: 0; padding-left: 20px; }
    ul.tree.root { padding-left: 0; }
    .node {
      display: flex; align-items: center; gap: 6px; padding: 4px 6px; border-radius: 8px; cursor: pointer;
      &:hover { background: var(--mat-sys-surface-container-high); }
      &.selected { background: var(--mat-sys-secondary-container); }
    }
    .toggle {
      width: 24px; height: 24px; border: 0; background: none; padding: 0; cursor: pointer;
      display: inline-flex; align-items: center; justify-content: center; color: inherit;
    }
    .toggle.leaf { display: inline-block; }
  `,
})
export class TreeView<T extends TreeNodeLike<T>> {
  readonly nodes = input.required<T[]>();
  readonly nodeTemplate = input.required<TemplateRef<{ $implicit: T }>>();
  readonly depth = input(0);
  readonly selectedId = model<number | null>(null);
  /** Ids of open nodes; shared by every level of the tree. */
  readonly expanded = model<Set<number>>(new Set());
  readonly picked = output<T>();

  protected isOpen(id: number): boolean {
    return this.expanded().has(id);
  }

  protected toggle(id: number): void {
    const next = new Set(this.expanded());
    if (next.has(id)) next.delete(id); else next.add(id);
    this.expanded.set(next);
  }

  protected select(node: T): void {
    this.selectedId.set(node.id);
    this.picked.emit(node);
  }
}

/** Ids of every node down to the given depth, for an initially expanded tree. */
export function expandToDepth<T extends TreeNodeLike<T>>(nodes: T[], maxDepth: number, depth = 0, out = new Set<number>()): Set<number> {
  if (depth >= maxDepth) return out;
  for (const n of nodes) {
    out.add(n.id);
    expandToDepth(n.children, maxDepth, depth + 1, out);
  }
  return out;
}

