package com.bracits.pms.shared.api;

/**
 * Bean Validation group for constraints that apply only on update (e.g. {@code version} is required).
 * Use with {@code @Validated({Default.class, OnUpdate.class})} on the request body.
 */
public interface OnUpdate {
}
