package org.example.spqr.models.domain;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder(builderClassName = "Builder", toBuilder = true)
public record Entity(
        String entityId,
        EntityStatus status,
        ZonedDateTime activatedAt,
        ScenarioSpec scenarioSpec,
        Preconditions preconditions) {
}
