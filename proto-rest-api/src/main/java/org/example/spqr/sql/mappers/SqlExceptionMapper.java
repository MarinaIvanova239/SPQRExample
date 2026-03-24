package org.example.spqr.sql.mappers;


import org.example.spqr.exceptions.ProtoException;

public interface SqlExceptionMapper {
    ProtoException toProtoException(final Exception originalException);
}