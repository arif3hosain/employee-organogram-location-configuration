/** Helpers for the location and org-unit trees returned by the backend (/tree endpoints). */

export interface TreeNodeLike<T> {
  id: number;
  children: T[];
}

/** A tree flattened into select options, indented by depth. */
export interface TreeOption {
  id: number;
  label: string;
  depth: number;
  levelNo: number;
}

export function flattenTree<T extends TreeNodeLike<T> & { levelNo: number }>(
  nodes: T[], label: (n: T) => string, depth = 0, out: TreeOption[] = [],
): TreeOption[] {
  for (const n of nodes) {
    out.push({ id: n.id, label: label(n), depth, levelNo: n.levelNo });
    flattenTree(n.children, label, depth + 1, out);
  }
  return out;
}

export function findNode<T extends TreeNodeLike<T>>(nodes: T[], id: number): T | null {
  for (const n of nodes) {
    if (n.id === id) return n;
    const hit = findNode(n.children, id);
    if (hit) return hit;
  }
  return null;
}

/** Ids of a node and everything under it (cannot be chosen as its new parent). */
export function subtreeIds<T extends TreeNodeLike<T>>(node: T | null): Set<number> {
  const ids = new Set<number>();
  const walk = (n: T) => { ids.add(n.id); n.children.forEach(walk); };
  if (node) walk(node);
  return ids;
}

/** Ids on the path from a root down to the node, so the tree can be expanded to show it. */
export function ancestorIds<T extends TreeNodeLike<T>>(nodes: T[], id: number, trail: number[] = []): number[] | null {
  for (const n of nodes) {
    if (n.id === id) return trail;
    const hit = ancestorIds(n.children, id, [...trail, n.id]);
    if (hit) return hit;
  }
  return null;
}

/** Visual indent for mat-option labels. */
export function indent(depth: number): string {
  return '   '.repeat(depth) + (depth ? '└ ' : '');
}
