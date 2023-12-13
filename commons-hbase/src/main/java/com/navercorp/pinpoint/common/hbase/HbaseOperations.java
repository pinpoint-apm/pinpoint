/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.hbase;

import com.navercorp.pinpoint.common.hbase.async.AdvancedAsyncTableCallback;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableCallback;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author emeroad
 * @author minwoo.jung
 * @author Taejin Koo
 */
public interface HbaseOperations {
    /**
     * Gets an individual row from the given table. The content is mapped by the given action.
     *
     * @param tableName target table
     * @param get    Get
     * @return object mapping the target row
     */
    <T> T get(TableName tableName, final Get get, final RowMapper<T> mapper);

    <T> List<T> get(TableName tableName, final List<Get> get, final RowMapper<T> mapper);

    void put(TableName tableName, final Put put);
    void put(TableName tableName, final List<Put> puts);


    /**
     * Atomically checks if a CheckAndMutate value matches the expected
     * value. If it does, it adds the put.  If the passed value is null, the check
     * is for the lack of column (ie: non-existence)
     *
     * @param tableName  target table
     * @param checkAndMutate CheckAndMutate
     */
    CheckAndMutateResult checkAndMutate(TableName tableName, CheckAndMutate checkAndMutate);

    List<CheckAndMutateResult> checkAndMutate(TableName tableName, List<CheckAndMutate> checkAndMutates);

    /**
     *
     * @param tableName  target table
     * @param rowName to check
     * @param familyName column family to check
     * @param qualifier column qualifier to check
     * @param value if the value provided is greater than the saved, update the saved
     */
    void maxColumnValue(TableName tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long value);

    void delete(TableName tableName, final Delete delete);
    void delete(TableName tableName, final List<Delete> deletes);

    <T> List<T> find(TableName tableName, final List<Scan> scans, final ResultsExtractor<T> action);
    <T> List<List<T>> find(TableName tableName, final List<Scan> scans, final RowMapper<T> action);

    <T> List<T> findParallel(TableName tableName, final List<Scan> scans, final ResultsExtractor<T> action);
    <T> List<List<T>> findParallel(TableName tableName, final List<Scan> scans, final RowMapper<T> action);

    // Distributed scanners
    <T> List<T> find(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final RowMapper<T> action);
    <T> List<T> find(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action);
    <T> List<T> find(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action, final LimitEventHandler limitEventHandler);
    <T> T find(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final ResultsExtractor<T> action);

    // Parallel scanners for distributed scans
    <T> List<T> findParallel(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final RowMapper<T> action, int numParallelThreads);
    <T> List<T> findParallel(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action, int numParallelThreads);
    <T> List<T> findParallel(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action, final LimitEventHandler limitEventHandler, int numParallelThreads);
    <T> T findParallel(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final ResultsExtractor<T> action, int numParallelThreads);

    Result increment(TableName tableName, final Increment increment);

    /**
     * Exception throwing can partially happen in case of incrementList. you will be accepted the last Exception.
     * If you want to retry something with catching a specific exception,
     * There is a limitation that you can't know which exception throws due to throwing always last exception.
     *
     * @param tableName
     * @param incrementList
     * @return
     */
    List<Result> increment(TableName tableName, final List<Increment> incrementList);

    long incrementColumnValue(TableName tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount);
    long incrementColumnValue(TableName tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount, final Durability durability);

    CompletableFuture<Result> asyncIncrement(final TableName tableName, final Increment incrementList);
    List<CompletableFuture<Result>> asyncIncrement(final TableName tableName, final List<Increment> incrementList);
    CompletableFuture<Long> asyncIncrement(TableName tableName, byte[] row, byte[] family, byte[] qualifier, long amount, Durability durability);


    /**
     * Executes the given action against the specified table handling resource management.
     * <p>
     * Application exceptions thrown by the action object get propagated to the caller (can only be unchecked). 
     * Allows for returning a result object (typically a domain object or collection of domain objects).
     * 
     * @param tableName the target table
     * @param action callback object that specifies the action
     * @param <T> action type
     * @return the result object of the callback action, or null
     */
    <T> T execute(TableName tableName, TableCallback<T> action);

    /**
     * Scans the target table using the given {@link Scan} object. Suitable for maximum control over the scanning
     * process.
     * The content is processed by the given action typically returning a domain object or collection of domain objects.
     * 
     * @param tableName target table
     * @param scan table scanner
     * @param action action handling the scanner results
     * @param <T> action type
     * @return the result object of the callback action, or null
     */
    <T> T find(TableName tableName, final Scan scan, final ResultsExtractor<T> action);

    /**
     * Scans the target table using the given {@link Scan} object. Suitable for maximum control over the scanning
     * process.
     * The content is processed row by row by the given action, returning a list of domain objects.
     * 
     * @param tableName target table
     * @param scan table scanner
     * @param action row mapper handling the scanner results
     * @param <T> action type
     * @return a list of objects mapping the scanned rows
     */
    <T> List<T> find(TableName tableName, final Scan scan, final RowMapper<T> action);


    <T> T asyncExecute(TableName tableName, AdvancedAsyncTableCallback<T> action);

    <T> T asyncExecute(TableName tableName, AsyncTableCallback<T> action);
}
