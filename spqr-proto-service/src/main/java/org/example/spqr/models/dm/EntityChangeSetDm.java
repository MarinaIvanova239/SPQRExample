package org.example.spqr.models.dm;

import lombok.Data;

import java.util.Date;

@Data
public class EntityChangeSetDm {
    private boolean statusUpdated;
    private Integer stateId;
    private Date updatedAt;

    private boolean activatedAtUpdated;
    private Date activatedAt;
}
