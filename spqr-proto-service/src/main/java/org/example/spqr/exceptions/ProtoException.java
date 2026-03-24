package org.example.spqr.exceptions;

public class ProtoException extends RuntimeException {
    private final String errorCode;

    private static String getErrorMessage(String errorMessage, Object... args) {
        return args == null ? errorMessage : String.format(errorMessage, args);
    }

    protected ProtoException(String errorCode, Object... args) {
        super(getErrorMessage(errorCode, args));
        this.errorCode = errorCode;
    }

    protected ProtoException(String errorCode, Throwable e) {
        super(getErrorMessage(errorCode), e);
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        return errorCode;
    }
}
