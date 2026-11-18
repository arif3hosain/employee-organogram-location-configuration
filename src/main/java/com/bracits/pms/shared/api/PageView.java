package com.bracits.pms.shared.api;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Stable JSON shape for paged lists (Spring's Page JSON is not a stable contract). */
public record PageView<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <E, T> PageView<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageView<>(page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
