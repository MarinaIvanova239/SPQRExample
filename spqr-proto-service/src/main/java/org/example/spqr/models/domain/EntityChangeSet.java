package org.example.spqr.models.domain;

import jakarta.annotation.Nullable;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Optional;

@Data
public class EntityChangeSet {
    @Nullable
    private Optional<EntityStatus> status;

    @Nullable
    private Optional<ZonedDateTime> activatedAt;

    public EntityChangeSet setStatus(EntityStatus status) {
        this.status = Optional.ofNullable(status);
        return this;
    }

    public EntityChangeSet setActivatedAt(ZonedDateTime activatedAt) {
        this.activatedAt = Optional.ofNullable(activatedAt);
        return this;
    }

}
