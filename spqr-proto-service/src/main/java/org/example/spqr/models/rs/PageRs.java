package org.example.spqr.models.rs;

import lombok.Data;

import java.util.List;

@Data
public class PageRs<T> {
    private ListInfoRs listInfo;
    private List<T> items;
}
