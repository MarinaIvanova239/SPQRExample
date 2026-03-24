package org.example.spqr.exceptions;


public class DbQueryExecutionException extends ProtoException {

    public DbQueryExecutionException(Throwable e) {
        super("query exception", e);
    }
}