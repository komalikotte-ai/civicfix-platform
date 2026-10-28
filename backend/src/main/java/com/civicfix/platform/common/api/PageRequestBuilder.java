package com.civicfix.platform.common.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Standard utility to build Spring Data Pageable requests with safety guards.
 */
public final class PageRequestBuilder {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "createdAt";

    private PageRequestBuilder() {
    }

    public static Pageable build(Integer page, Integer size, String sortBy, String sortDirection) {
        int pageIndex = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int pageSize = (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        String field = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_FIELD : sortBy;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(pageIndex, pageSize, Sort.by(direction, field));
    }

    public static Pageable build(Integer page, Integer size) {
        return build(page, size, DEFAULT_SORT_FIELD, "desc");
    }
}
