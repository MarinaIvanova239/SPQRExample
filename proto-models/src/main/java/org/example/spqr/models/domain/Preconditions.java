package org.example.spqr.models.domain;

import lombok.Builder;

import java.util.List;

@Builder(builderClassName = "Builder", toBuilder = true)
public record Preconditions(
        List<String> entityIds,
        int subscriptionsCount) {
}
