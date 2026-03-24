package org.example.spqr.models.domain;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder(builderClassName = "Builder", toBuilder = true)
public record Request(
        String entityId,
        ZonedDateTime entityCreatedAt,
        String requestId,
        ZonedDateTime createdAt) {
}
