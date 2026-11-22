package com.bracits.pms.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pure domain rules: no Spring context, runs in milliseconds. */
class HierarchyPathTest {

    @Test
    void childPathOfRootAndOfChild() {
        assertThat(HierarchyPath.childPath(null, 1)).isEqualTo("/1/");
        assertThat(HierarchyPath.childPath("/1/5/", 12)).isEqualTo("/1/5/12/");
    }

    @Test
    void detectsMoveUnderOwnSubtree() {
        assertThat(HierarchyPath.isSelfOrDescendant("/1/5/12/", "/1/5/")).isTrue();   // child
        assertThat(HierarchyPath.isSelfOrDescendant("/1/5/", "/1/5/")).isTrue();      // itself
        assertThat(HierarchyPath.isSelfOrDescendant("/1/6/", "/1/5/")).isFalse();     // sibling
        assertThat(HierarchyPath.isSelfOrDescendant("/1/55/", "/1/5/")).isFalse();    // trailing slash prevents 5 vs 55 confusion
    }

    @Test
    void rebasesDescendantAfterMove() {
        assertThat(HierarchyPath.rebase("/1/5/12/30/", "/1/5/12/", "/1/6/12/")).isEqualTo("/1/6/12/30/");
        assertThatThrownBy(() -> HierarchyPath.rebase("/9/", "/1/", "/2/")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void depthCountsLevels() {
        assertThat(HierarchyPath.depth("/1/")).isEqualTo(1);
        assertThat(HierarchyPath.depth("/1/5/12/")).isEqualTo(3);
    }

    @Test
    void levelRules() {
        assertThat(HierarchyPath.checkLevel(1, null, "location")).isNull();            // root type, no parent
        assertThat(HierarchyPath.checkLevel(1, 1, "location")).contains("cannot have a parent");
        assertThat(HierarchyPath.checkLevel(3, null, "location")).contains("Parent is required");
        assertThat(HierarchyPath.checkLevel(4, 2, "location")).isNull();               // skipping a level is allowed
        assertThat(HierarchyPath.checkLevel(2, 3, "location")).contains("higher level");
        assertThat(HierarchyPath.checkLevel(3, 3, "location")).contains("higher level");
    }
}
