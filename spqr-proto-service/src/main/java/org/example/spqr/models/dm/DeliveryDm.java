package org.example.spqr.models.dm;

import lombok.Data;

import java.util.Date;

@Data
public class DeliveryDm {
    String entityId;
    Date entityCreatedAt;
    Date deliveredAt;
    String subscriptionId;
}
