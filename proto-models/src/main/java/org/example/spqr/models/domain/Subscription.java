package org.example.spqr.models.domain;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder(builderClassName = "Builder")
public record Subscription(
        String entityId,
        ZonedDateTime entityCreatedAt,
        ZonedDateTime createdAt,
        SubscriberInfo subscriberInfo,
        String subscriptionId) {
}
