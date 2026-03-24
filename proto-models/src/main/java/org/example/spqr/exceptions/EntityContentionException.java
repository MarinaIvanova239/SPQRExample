package org.example.spqr.exceptions;

import java.util.List;

public class EntityContentionException extends ProtoException {

    public EntityContentionException(String externalId) {
        super("concurrent modification", externalId);
    }

    public EntityContentionException(Throwable e) {
        super("lock contention", e);
    }

    public EntityContentionException(List<String> externalIds) {
        super("concurrent modification", externalIds);
    }
}
