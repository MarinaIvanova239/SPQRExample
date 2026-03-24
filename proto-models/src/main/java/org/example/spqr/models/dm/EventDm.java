package org.example.spqr.models.dm;

import lombok.Data;

import java.util.Date;

@Data
public class EventDm {
    String entityId;
    Date entityCreatedAt;
    String eventName;
    Date registeredAt;
}
