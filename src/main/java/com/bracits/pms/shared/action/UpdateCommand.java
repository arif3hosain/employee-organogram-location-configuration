package com.bracits.pms.shared.action;

/** Input for update actions: the id from the URL plus the request body. */
public record UpdateCommand<F>(Long id, F form) {
}
