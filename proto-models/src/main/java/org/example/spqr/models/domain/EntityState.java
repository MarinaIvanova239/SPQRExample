package org.example.spqr.models.domain;

public enum EntityState {
    NEW(1),
    SUSPENDED(2),
    UNKNOWN(42);

    private final int value;

    EntityState(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static EntityState toEntityState(Integer value) {
        for (EntityState e : EntityState.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
