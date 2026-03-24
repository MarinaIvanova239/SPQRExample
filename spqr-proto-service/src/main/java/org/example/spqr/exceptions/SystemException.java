package org.example.spqr.exceptions;

public class SystemException extends ProtoException {

    public SystemException(Throwable e) {
        super("system error", e);
    }
}
