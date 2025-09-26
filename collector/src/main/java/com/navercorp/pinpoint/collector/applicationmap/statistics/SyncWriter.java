/*
 * Copyright 2025 NAVER Corp.
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
 */

package com.navercorp.pinpoint.collector.applicationmap.statistics;

import com.navercorp.pinpoint.common.hbase.CheckAndMax;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncTemplate;
import com.navercorp.pinpoint.common.hbase.util.Increments;
import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import io.lettuce.core.internal.Futures;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Result;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author emeroad
 */
public class SyncWriter implements BulkWriter {

    private static final Duration awaitTimeout = Duration.ofMillis(100);

    private final HbaseAsyncTemplate hbaseTemplate;
    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;
    private final byte[] family;

    public SyncWriter(String loggerName,
                            HbaseAsyncTemplate hbaseTemplate,
                             byte[] family,
                             RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.family = Objects.requireNonNull(family, "family");
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
    }

    @Override
    public void increment(TableName tableName, RowKey rowKey, ColumnName columnName) {
        Objects.requireNonNull(rowKey, "rowKey");
        Objects.requireNonNull(columnName, "columnName");

        this.increment(tableName, rowKey, columnName, 1L);
    }

    @Override
    public void increment(TableName tableName, RowKey rowKey, ColumnName columnName, long addition) {
        Objects.requireNonNull(rowKey, "rowKey");
        Objects.requireNonNull(columnName, "columnName");

        final byte[] rowKeyBytes = getDistributedKey(rowKey);

        Increment increment = Increments.increment(rowKeyBytes, family, columnName.getColumnName(), 1);
        increment.setReturnResults(false);

        CompletableFuture<Result> future = this.hbaseTemplate.increment(tableName, increment);
        Futures.await(awaitTimeout, future);
    }

    @Override
    public void updateMax(TableName tableName, RowKey rowKey, ColumnName columnName, long max) {

        Objects.requireNonNull(rowKey, "rowKey");
        Objects.requireNonNull(columnName, "columnName");

        final byte[] rowKeyBytes = getDistributedKey(rowKey);
        CheckAndMax checkAndMax = new CheckAndMax(rowKeyBytes, family, columnName.getColumnName(), max);
        this.hbaseTemplate.maxColumnValue(tableName, checkAndMax);
    }

    @Override
    public void flushLink() {
        // empty
    }

    @Override
    public void flushAvgMax() {
        // empty
    }

    private byte[] getDistributedKey(RowKey rowKey) {
        ByteHasher byteHasher = this.rowKeyDistributorByHashPrefix.getByteHasher();
        byte[] bytes = rowKey.getRowKey(byteHasher.getSaltKey().size());
        return byteHasher.writeSaltKey(bytes);
    }
}
