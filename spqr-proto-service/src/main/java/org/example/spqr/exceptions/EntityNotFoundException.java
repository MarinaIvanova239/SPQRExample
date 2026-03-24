package org.example.spqr.exceptions;

public class EntityNotFoundException extends ProtoException {
    public EntityNotFoundException() {
        super("entity not found");
    }
}
