package com.navercorp.pinpoint.metric.common.pinot;

import org.apache.pinot.client.PinotResultSet;
import org.apache.pinot.client.ResultSetGroup;

import java.sql.ResultSet;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ResultSetFuture implements Future<ResultSet> {
    private final Future<ResultSetGroup> resultSetGroupFuture;

    public ResultSetFuture(Future<ResultSetGroup> resultSetGroupFuture) {
        this.resultSetGroupFuture = Objects.requireNonNull(resultSetGroupFuture, "resultSetGroupFuture");
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.resultSetGroupFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return this.resultSetGroupFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.resultSetGroupFuture.isDone();
    }

    @Override
    public ResultSet get() throws InterruptedException, ExecutionException {
        ResultSetGroup resultSetGroup = this.resultSetGroupFuture.get();
        return toResultSet(resultSetGroup);
    }

    @Override
    public ResultSet get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        ResultSetGroup resultSetGroup = this.resultSetGroupFuture.get(timeout, unit);
        return toResultSet(resultSetGroup);
    }

    private static PinotResultSet toResultSet(ResultSetGroup resultSetGroup) {
        if (resultSetGroup == null) {
//            return null or empty??
            return PinotResultSet.empty();
        }
        if (resultSetGroup.getResultSetCount() == 0) {
            return PinotResultSet.empty();
        }
        return new PinotResultSet(resultSetGroup.getResultSet(0));
    }
}
