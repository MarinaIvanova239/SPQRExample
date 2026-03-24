package org.example.spqr.sql.transaction;

import org.example.spqr.exceptions.DbInfrastructureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;


public class SqlTransactionManager implements PlatformTransactionManager {

    private final PlatformTransactionManager target;

    public SqlTransactionManager(PlatformTransactionManager target) {
        this.target = target;
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) {
        try {
            return target.getTransaction(definition);

        } catch (TransactionException e) {
            throw new DbInfrastructureException(e);
        }
    }

    @Override
    public void commit(TransactionStatus status) {
        try {
            target.commit(status);

        } catch (TransactionException e) {
            throw new DbInfrastructureException(e);
        }
    }

    @Override
    public void rollback(TransactionStatus status) {
        try {
            target.rollback(status);

        } catch (TransactionException e) {
            throw new DbInfrastructureException(e);
        }
    }
}