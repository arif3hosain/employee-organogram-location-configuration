package com.bracits.pms.shared.domain;

/** Stored as VARCHAR (not BOOLEAN) so the same DDL ports to Oracle, which lacks BOOLEAN before 23ai. */
public enum RecordStatus {
    ACTIVE,
    INACTIVE
}
