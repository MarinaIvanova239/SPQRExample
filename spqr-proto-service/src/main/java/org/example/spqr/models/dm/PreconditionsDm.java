package org.example.spqr.models.dm;

import lombok.Data;

import java.util.List;

@Data
public class PreconditionsDm {
    List<String> entityIds;
    int subscriptionsCount;
}
