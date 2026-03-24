package org.example.spqr.models.dm;

import lombok.Data;

import java.util.Date;

@Data
public class EntityDm {
    String entityId;
    int stateId;
    Date createdAt;
    Date updatedAt;
    Date activatedAt;
    String scenarioId;
    String scenarioName;
    String preconditions;
}
