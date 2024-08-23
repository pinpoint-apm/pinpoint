package com.navercorp.pinpoint.common.hbase.async;

import com.navercorp.pinpoint.common.hbase.CasResult;
import com.navercorp.pinpoint.common.hbase.CheckAndMax;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AsyncHbaseOperations {
    <T> T advancedExecute(TableName tableName, AdvancedAsyncTableCallback<T> action);

    <T> T execute(TableName tableName, AsyncTableCallback<T> action);


    CompletableFuture<Void> put(TableName tableName, Put put);

    List<CompletableFuture<Void>> put(TableName tableName, List<Put> puts);


    <T> CompletableFuture<T> get(TableName tableName, Get get, RowMapper<T> mapper);

    <T> List<CompletableFuture<T>> get(TableName tableName, final List<Get> gets, final RowMapper<T> mapper);


    CompletableFuture<Void> delete(TableName tableName, final Delete delete);


    default CompletableFuture<Long> increment(TableName tableName, byte[] row, byte[] family, byte[] qualifier, long amount) {
        return increment(tableName, row, family, qualifier, amount, Durability.SKIP_WAL);
    }

    default CompletableFuture<Long> increment(TableName tableName, byte[] row, byte[] family, byte[] qualifier, long amount, Durability durability) {
        Increment increment = new Increment(row).
                addColumn(family, qualifier, amount).
                setDurability(durability);

        return increment(tableName, increment).thenApply((r) -> {
            return Bytes.toLong(r.getValue(family, qualifier));
        });
    }


    CompletableFuture<Result> increment(TableName tableName, Increment increment);

    List<CompletableFuture<Result>> increment(TableName tableName, List<Increment> incrementList);


    CompletableFuture<CasResult> maxColumnValue(TableName tableName, CheckAndMax max);

    List<CompletableFuture<CheckAndMutateResult>> checkAndMutate(TableName tableName, List<CheckAndMutate> checkAndMutates);
}
