package com.bracits.pms.corehr.organogram.domain;

/**
 * Whether a unit does the organization's core (revenue / programme) work or supports it.
 * FUNCTIONAL     e.g. Technology, Programme, Sales
 * NON_FUNCTIONAL e.g. HR, Finance, Admin, Audit (support)
 * [Assumption, see POC_NOTES.md Q4]
 */
public enum OrgUnitNature {
    FUNCTIONAL,
    NON_FUNCTIONAL
}
