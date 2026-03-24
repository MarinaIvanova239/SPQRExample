package org.example.spqr.exceptions;

public class DbInfrastructureException extends ProtoException {

    public DbInfrastructureException(Throwable e) {
        super("db exception happened", e);
    }
}
