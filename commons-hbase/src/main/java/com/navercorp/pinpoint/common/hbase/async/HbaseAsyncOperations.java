/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.common.hbase.async;

import com.navercorp.pinpoint.common.hbase.LimitEventHandler;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;

import java.util.List;

public interface HbaseAsyncOperations {


    <T> List<T> findParallel(TableName tableName, final List<Scan> scans, final ResultsExtractor<T> action);
    <T> List<List<T>> findParallel(TableName tableName, final List<Scan> scans, final RowMapper<T> action);

    // Parallel scanners for distributed scans
    <T> List<T> findParallel(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final RowMapper<T> action, int numParallelThreads);
    <T> List<T> findParallel(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action, int numParallelThreads);
    <T> List<T> findParallel(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action, final LimitEventHandler limitEventHandler, int numParallelThreads);
    <T> T findParallel(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final ResultsExtractor<T> action, int numParallelThreads);

    <T> T executeAsync(TableName tableName, AsyncTableCallback<T> action);
}
