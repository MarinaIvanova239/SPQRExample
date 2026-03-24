package org.example.spqr.models.domain;

import lombok.Builder;
import lombok.NonNull;

import java.time.ZonedDateTime;

@Builder(builderClassName = "Builder", toBuilder = true)
public record Delivery(
        String entityId,
        ZonedDateTime entityCreatedAt,
        ZonedDateTime deliveredAt,
        String subscriptionId) {

    public static Delivery create(@NonNull Subscription subscription, ZonedDateTime deliveryTime) {
        return Delivery.builder()
                .entityId(subscription.subscriberInfo().entityId())
                .entityCreatedAt(subscription.subscriberInfo().entityCreatedAt())
                .subscriptionId(subscription.subscriptionId())
                .deliveredAt(deliveryTime)
                .build();
    }
}
