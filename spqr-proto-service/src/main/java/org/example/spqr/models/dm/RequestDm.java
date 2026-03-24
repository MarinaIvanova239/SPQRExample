package org.example.spqr.models.dm;

import lombok.Data;

import java.util.Date;

@Data
public class RequestDm {
    String entityId;
    Date entityCreatedAt;
    String requestId;
    Date createdAt;
}
