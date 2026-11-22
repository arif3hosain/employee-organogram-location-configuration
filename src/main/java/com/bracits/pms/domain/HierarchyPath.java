package com.bracits.pms.domain;

/**
 * Pure helpers for materialized-path trees (Location, OrgUnit).
 * A node's path is "/rootId/.../parentId/ownId/", so a subtree is "path LIKE '/1/5/%'".
 * No Spring, no DB: unit-tested in HierarchyPathTest.
 */
public final class HierarchyPath {

    private HierarchyPath() {
    }

    /** Path for a node with the given id under a parent path (null parent = root). */
    public static String childPath(String parentPath, long id) {
        return (parentPath == null ? "/" : parentPath) + id + "/";
    }

    /** True when candidate is the node itself or one of its descendants (moving there would create a cycle). */
    public static boolean isSelfOrDescendant(String candidatePath, String nodePath) {
        return candidatePath != null && nodePath != null && candidatePath.startsWith(nodePath);
    }

    /** Replace the old prefix of a descendant's path with the new one after a move. */
    public static String rebase(String path, String oldPrefix, String newPrefix) {
        if (!path.startsWith(oldPrefix)) {
            throw new IllegalArgumentException("Path " + path + " is not under " + oldPrefix);
        }
        return newPrefix + path.substring(oldPrefix.length());
    }

    /** Number of levels below the root (root = 1). */
    public static int depth(String path) {
        if (path == null || path.length() < 2) {
            return 0;
        }
        return path.substring(1, path.length() - 1).split("/").length;
    }

    /**
     * Level rule shared by every typed hierarchy: a node of a top-level type (level 1) is a root
     * and has no parent; any other node needs a parent whose type level is strictly smaller.
     * Levels may be skipped (e.g. an office directly under a District).
     *
     * @return null when valid, otherwise an error message
     */
    public static String checkLevel(int nodeLevel, Integer parentLevel, String what) {
        if (nodeLevel == 1 && parentLevel != null) {
            return "A top-level " + what + " cannot have a parent";
        }
        if (nodeLevel > 1 && parentLevel == null) {
            return "Parent is required for this " + what + " type";
        }
        if (parentLevel != null && parentLevel >= nodeLevel) {
            return "Parent " + what + " type must be at a higher level (smaller level number) than the child";
        }
        return null;
    }
}
