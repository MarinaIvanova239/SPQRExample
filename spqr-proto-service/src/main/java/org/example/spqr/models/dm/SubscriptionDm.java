package org.example.spqr.models.dm;

import lombok.Data;

import java.util.Date;

@Data
public class SubscriptionDm {
    String entityId;
    Date entityCreatedAt;
    Date createdAt;
    String subscriberInfo;
    String subscriptionId;
}
