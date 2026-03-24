package org.example.spqr.models.domain;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class Page<T> {
    private final int limit;
    private final int offset;
    private final List<T> items;

    public Page(List<T> items, int limit, int offset) {
        this.items = new ImmutableList.Builder<T>().addAll(items).build();
        this.limit = limit;
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public List<T> getItems() {
        return items;
    }
}
