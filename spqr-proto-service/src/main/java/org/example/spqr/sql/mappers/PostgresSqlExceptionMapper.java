package org.example.spqr.sql.mappers;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.example.spqr.exceptions.DbInfrastructureException;
import org.example.spqr.exceptions.DbQueryExecutionException;
import org.example.spqr.exceptions.ProtoException;
import org.example.spqr.exceptions.SystemException;
import org.example.spqr.exceptions.EntityContentionException;
import org.postgresql.util.PSQLException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component("sqlExceptionMapper")
public class PostgresSqlExceptionMapper implements SqlExceptionMapper {

    public ProtoException toProtoException(final Exception originalException) {
        if (originalException instanceof ProtoException) {
            return (ProtoException) originalException;
        }

        List<Throwable> throwableList = ExceptionUtils.getThrowableList(originalException);
        for (Throwable throwable : throwableList) {
            if (throwable instanceof CannotGetJdbcConnectionException) {
                return new DbInfrastructureException(originalException);
            }
            if (throwable instanceof DataAccessResourceFailureException) {
                return new DbInfrastructureException(originalException);
            }
            if (throwable instanceof BadSqlGrammarException) {
                return new DbQueryExecutionException(originalException);
            }
            if (throwable instanceof CannotAcquireLockException) {
                return new EntityContentionException(originalException);
            }
            if (throwable instanceof OptimisticLockingFailureException) {
                return new EntityContentionException(originalException);
            }
            if (throwable instanceof DeadlockLoserDataAccessException) {
                return new EntityContentionException(originalException);
            }
        }

        for (Throwable throwable : throwableList) {
            if (isInfrastructureThrowable(throwable)) {
                return new DbInfrastructureException(originalException);
            }
        }

        return new SystemException(originalException);
    }

    private static final Set<String> POSTGRE_INFRASTRUCTURE_SQLSTATE = ImmutableSet.of("53000", "53100", "53200", "53300",
            "53400", "57014", "23514", "25006");

    protected boolean isInfrastructureThrowable(Throwable throwable) {
        if (throwable instanceof PSQLException) {
            return POSTGRE_INFRASTRUCTURE_SQLSTATE.contains(((PSQLException) throwable).getSQLState());
        }
        return false;
    }
}
