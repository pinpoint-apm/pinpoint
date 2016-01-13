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

import java.util.List;

import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.client.*;
import org.springframework.data.hadoop.hbase.HbaseOperations;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.RowMapper;

/**
 * @author emeroad
 */
public interface HbaseOperations2 extends HbaseOperations {
    /**
     * Gets an individual row from the given table. The content is mapped by the given action.
     *
     * @param tableName target table
     * @param rowName   row name
     * @param mapper    row mapper
     * @return object mapping the target row
     */
    <T> T get(String tableName, byte[] rowName, final RowMapper<T> mapper);

    /**
     * Gets an individual row from the given table. The content is mapped by the given action.
     *
     * @param tableName  target table
     * @param rowName    row name
     * @param familyName column family
     * @param mapper     row mapper
     * @return object mapping the target row
     */
    <T> T get(String tableName, byte[] rowName, byte[] familyName, final RowMapper<T> mapper);

    /**
     * Gets an individual row from the given table. The content is mapped by the given action.
     *
     * @param tableName  target table
     * @param rowName    row name
     * @param familyName family
     * @param qualifier  column qualifier
     * @param mapper     row mapper
     * @return object mapping the target row
     */
    <T> T get(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final RowMapper<T> mapper);

    <T> T get(String tableName, final Get get, final RowMapper<T> mapper);

    <T> List<T> get(String tableName, final List<Get> get, final RowMapper<T> mapper);


    void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final byte[] value);

    void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final Long timestamp, final byte[] value);

    <T> void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final T value, final ValueMapper<T> mapper);

    <T> void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final Long timestamp, final T value, final ValueMapper<T> mapper);

    void put(String tableName, final Put put);

    void put(String tableName, final List<Put> puts);

    void delete(String tableName, final Delete delete);

    void delete(String tableName, final List<Delete> deletes);

    <T> List<T> find(String tableName, final List<Scan> scans, final ResultsExtractor<T> action);

    <T> List<List<T>> find(String tableName, final List<Scan> scans, final RowMapper<T> action);

    <T> List<T> findParallel(String tableName, final List<Scan> scans, final ResultsExtractor<T> action);

    <T> List<List<T>> findParallel(String tableName, final List<Scan> scans, final RowMapper<T> action);

    // Distributed scanners

    <T> List<T> find(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final RowMapper<T> action);

    <T> List<T> find(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action);

    <T> List<T> find(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action, final LimitEventHandler limitEventHandler);

    <T> T find(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final ResultsExtractor<T> action);

    // Parallel scanners for distributed scans

    <T> List<T> findParallel(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final RowMapper<T> action, int numParallelThreads);

    <T> List<T> findParallel(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action, int numParallelThreads);

    <T> List<T> findParallel(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action, final LimitEventHandler limitEventHandler, int numParallelThreads);

    <T> T findParallel(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final ResultsExtractor<T> action, int numParallelThreads);

    Result increment(String tableName, final Increment increment);

    /**
     * Exception throwing can partially happen in case of incrementList. you will be accepted the last Exception.
     * If you want to retry something with catching a specific exception,
     * There is a limitation that you can't know which exception throws due to throwing always last exception.
     *
     * @param tableName
     * @param incrementList
     * @return
     */
    List<Result> increment(String tableName, final List<Increment> incrementList);

    long incrementColumnValue(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount);

    long incrementColumnValue(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount, final boolean writeToWAL);

}
