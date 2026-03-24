package org.example.spqr.models.domain;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder(builderClassName = "Builder", toBuilder = true)
public record EntityStatus(
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        EntityState state) {
}
