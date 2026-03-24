package org.example.spqr.models.domain;

import lombok.Builder;

@Builder(builderClassName = "Builder", toBuilder = true)
public record ScenarioSpec(
        String id,
        String name) {
}
