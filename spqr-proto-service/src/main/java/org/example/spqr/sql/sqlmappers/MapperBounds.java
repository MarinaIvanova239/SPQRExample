package org.example.spqr.sql.sqlmappers;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class MapperBounds {

    int limit;
    int offset;
    int max;

    public MapperBounds(int limit, int offset) {
        this(limit, offset, offset + limit);
    }

}