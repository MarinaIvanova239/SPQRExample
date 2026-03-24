package org.example.spqr.models.domain;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder(builderClassName = "Builder")
public record SubscriberInfo(
        String entityId,
        ZonedDateTime entityCreatedAt) {
}
