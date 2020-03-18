package com.navercorp.pinpoint.common.hbase;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;

import java.util.Collections;
import java.util.List;

public class AsyncPutHbaseTemplate2 implements HbaseOperations2 {
    private final HbaseOperations2 delegate;

    public AsyncPutHbaseTemplate2(HbaseOperations2 delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean asyncPut(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, byte[] value) {
        final boolean success = delegate.asyncPut(tableName, rowName, familyName, qualifier, value);
        if (success) {
            return true;
        }

        delegate.put(tableName, rowName, familyName, qualifier, value);
        return true;
    }

    @Override
    public boolean asyncPut(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, Long timestamp, byte[] value) {
        final boolean success = delegate.asyncPut(tableName, rowName, familyName, qualifier, timestamp, value);
        if (success) {
            return true;
        }

        delegate.put(tableName, rowName, familyName, qualifier, timestamp, value);
        return true;
    }

    @Override
    public <T> boolean asyncPut(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, T value, ValueMapper<T> mapper) {
        final boolean success = delegate.asyncPut(tableName, rowName, familyName, qualifier, value, mapper);
        if (success) {
            return true;
        }

        delegate.put(tableName, rowName, familyName, qualifier, value, mapper);
        return true;
    }

    @Override
    public <T> boolean asyncPut(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, Long timestamp, T value, ValueMapper<T> mapper) {
        final boolean success = delegate.asyncPut(tableName, rowName, familyName, qualifier, timestamp, value, mapper);
        if (success) {
            return true;
        }

        delegate.put(tableName, rowName, familyName, qualifier, timestamp, value, mapper);
        return true;
    }

    @Override
    public boolean asyncPut(TableName tableName, Put put) {
        final boolean success = delegate.asyncPut(tableName, put);
        if (success) {
            return true;
        }

        delegate.put(tableName, put);
        return true;
    }

    @Override
    public List<Put> asyncPut(TableName tableName, List<Put> puts) {
        List<Put> rejectedPuts = this.delegate.asyncPut(tableName, puts);
        if (CollectionUtils.hasLength(rejectedPuts)) {
            this.delegate.put(tableName, rejectedPuts);
        }
        return Collections.emptyList();
    }


    @Override
    public <T> T get(TableName tableName, byte[] rowName, RowMapper<T> mapper) {
        return delegate.get(tableName, rowName, mapper);
    }

    @Override
    public <T> T get(TableName tableName, byte[] rowName, byte[] familyName, RowMapper<T> mapper) {
        return delegate.get(tableName, rowName, familyName, mapper);
    }

    @Override
    public <T> T get(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, RowMapper<T> mapper) {
        return delegate.get(tableName, rowName, familyName, qualifier, mapper);
    }

    @Override
    public <T> T get(TableName tableName, Get get, RowMapper<T> mapper) {
        return delegate.get(tableName, get, mapper);
    }

    @Override
    public <T> List<T> get(TableName tableName, List<Get> get, RowMapper<T> mapper) {
        return delegate.get(tableName, get, mapper);
    }

    @Override
    public void put(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, byte[] value) {
        delegate.put(tableName, rowName, familyName, qualifier, value);
    }

    @Override
    public void put(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, Long timestamp, byte[] value) {
        delegate.put(tableName, rowName, familyName, qualifier, timestamp, value);
    }

    @Override
    public <T> void put(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, T value, ValueMapper<T> mapper) {
        delegate.put(tableName, rowName, familyName, qualifier, value, mapper);
    }

    @Override
    public <T> void put(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, Long timestamp, T value, ValueMapper<T> mapper) {
        delegate.put(tableName, rowName, familyName, qualifier, timestamp, value, mapper);
    }

    @Override
    public void put(TableName tableName, Put put) {
        delegate.put(tableName, put);
    }

    @Override
    public void put(TableName tableName, List<Put> puts) {
        delegate.put(tableName, puts);
    }

    @Override
    public void delete(TableName tableName, Delete delete) {
        delegate.delete(tableName, delete);
    }

    @Override
    public void delete(TableName tableName, List<Delete> deletes) {
        delegate.delete(tableName, deletes);
    }

    @Override
    public <T> List<T> find(TableName tableName, List<Scan> scans, ResultsExtractor<T> action) {
        return delegate.find(tableName, scans, action);
    }

    @Override
    public <T> List<List<T>> find(TableName tableName, List<Scan> scans, RowMapper<T> action) {
        return delegate.find(tableName, scans, action);
    }

    @Override
    public <T> List<T> findParallel(TableName tableName, List<Scan> scans, ResultsExtractor<T> action) {
        return delegate.findParallel(tableName, scans, action);
    }

    @Override
    public <T> List<List<T>> findParallel(TableName tableName, List<Scan> scans, RowMapper<T> action) {
        return delegate.findParallel(tableName, scans, action);
    }

    @Override
    public <T> List<T> find(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, RowMapper<T> action) {
        return delegate.find(tableName, scan, rowKeyDistributor, action);
    }

    @Override
    public <T> List<T> find(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, int limit, RowMapper<T> action) {
        return delegate.find(tableName, scan, rowKeyDistributor, limit, action);
    }

    @Override
    public <T> List<T> find(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, int limit, RowMapper<T> action, LimitEventHandler limitEventHandler) {
        return delegate.find(tableName, scan, rowKeyDistributor, limit, action, limitEventHandler);
    }

    @Override
    public <T> T find(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, ResultsExtractor<T> action) {
        return delegate.find(tableName, scan, rowKeyDistributor, action);
    }

    @Override
    public <T> List<T> findParallel(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, RowMapper<T> action, int numParallelThreads) {
        return delegate.findParallel(tableName, scan, rowKeyDistributor, action, numParallelThreads);
    }

    @Override
    public <T> List<T> findParallel(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, int limit, RowMapper<T> action, int numParallelThreads) {
        return delegate.findParallel(tableName, scan, rowKeyDistributor, limit, action, numParallelThreads);
    }

    @Override
    public <T> List<T> findParallel(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, int limit, RowMapper<T> action, LimitEventHandler limitEventHandler, int numParallelThreads) {
        return delegate.findParallel(tableName, scan, rowKeyDistributor, limit, action, limitEventHandler, numParallelThreads);
    }

    @Override
    public <T> T findParallel(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, ResultsExtractor<T> action, int numParallelThreads) {
        return delegate.findParallel(tableName, scan, rowKeyDistributor, action, numParallelThreads);
    }

    @Override
    public Result increment(TableName tableName, Increment increment) {
        return delegate.increment(tableName, increment);
    }

    @Override
    public List<Result> increment(TableName tableName, List<Increment> incrementList) {
        return delegate.increment(tableName, incrementList);
    }

    @Override
    public long incrementColumnValue(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, long amount) {
        return delegate.incrementColumnValue(tableName, rowName, familyName, qualifier, amount);
    }

    @Override
    public long incrementColumnValue(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, long amount, boolean writeToWAL) {
        return delegate.incrementColumnValue(tableName, rowName, familyName, qualifier, amount, writeToWAL);
    }

    @Override
    public <T> T execute(TableName tableName, TableCallback<T> action) {
        return delegate.execute(tableName, action);
    }

    @Override
    public <T> T find(TableName tableName, String family, ResultsExtractor<T> action) {
        return delegate.find(tableName, family, action);
    }

    @Override
    public <T> T find(TableName tableName, String family, String qualifier, ResultsExtractor<T> action) {
        return delegate.find(tableName, family, qualifier, action);
    }

    @Override
    public <T> T find(TableName tableName, Scan scan, ResultsExtractor<T> action) {
        return delegate.find(tableName, scan, action);
    }

    @Override
    public <T> List<T> find(TableName tableName, String family, RowMapper<T> action) {
        return delegate.find(tableName, family, action);
    }

    @Override
    public <T> List<T> find(TableName tableName, String family, String qualifier, RowMapper<T> action) {
        return delegate.find(tableName, family, qualifier, action);
    }

    @Override
    public <T> List<T> find(TableName tableName, Scan scan, RowMapper<T> action) {
        return delegate.find(tableName, scan, action);
    }

    @Override
    public <T> T get(TableName tableName, String rowName, RowMapper<T> mapper) {
        return delegate.get(tableName, rowName, mapper);
    }

    @Override
    public <T> T get(TableName tableName, String rowName, String familyName, RowMapper<T> mapper) {
        return delegate.get(tableName, rowName, familyName, mapper);
    }

    @Override
    public <T> T get(TableName tableName, String rowName, String familyName, String qualifier, RowMapper<T> mapper) {
        return delegate.get(tableName, rowName, familyName, qualifier, mapper);
    }
}
