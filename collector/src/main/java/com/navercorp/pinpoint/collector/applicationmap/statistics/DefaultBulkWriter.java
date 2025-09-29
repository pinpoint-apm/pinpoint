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
import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import org.apache.commons.collections4.ListUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author emeroad
 */
public class DefaultBulkWriter implements BulkWriter {

    private final Logger logger;

    private final byte[] family;
    private final ByteHasher hasher;

    private final BulkIncrementer bulkIncrementer;

    private final BulkUpdater bulkUpdater;

    private final RowKeyMerge merge ;

    private final HbaseAsyncTemplate asyncTemplate;
    private int batchSize = 200;

    public DefaultBulkWriter(String loggerName,
                             HbaseAsyncTemplate asyncTemplate,
                             byte[] family,
                             ByteHasher hasher,
                             BulkIncrementer bulkIncrementer,
                             BulkUpdater bulkUpdater) {
        this.logger = LogManager.getLogger(loggerName);
        this.family = Objects.requireNonNull(family, "family");
        this.asyncTemplate = Objects.requireNonNull(asyncTemplate, "asyncTemplate");

        this.hasher = Objects.requireNonNull(hasher, "hasher");
        this.merge = new RowKeyMerge(this::getDistributedKey);
        this.bulkIncrementer = Objects.requireNonNull(bulkIncrementer, "bulkIncrementer");
        this.bulkUpdater = Objects.requireNonNull(bulkUpdater, "bulkUpdater");
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public void increment(TableName tableName, RowKey rowKey, ColumnName columnName) {
        this.bulkIncrementer.increment(tableName, rowKey, columnName);
    }

    @Override
    public void increment(TableName tableName, RowKey rowKey, ColumnName columnName, long addition) {
        this.bulkIncrementer.increment(tableName, rowKey, columnName, addition);
    }

    @Override
    public void updateMax(TableName tableName, RowKey rowKey, ColumnName columnName, long max) {
        this.bulkUpdater.updateMax(tableName, rowKey, columnName, max);
    }

    @Override
    public void flushLink() {

        // update statistics by rowkey and column for now. need to update it by rowkey later.
        Map<RowInfo, Long> snapshot = bulkIncrementer.getIncrements();
        if (snapshot.isEmpty()) {
            return;
        }

        Map<TableName, List<Increment>> incrementMap = merge.createBulkIncrement(snapshot, family);

        for (Map.Entry<TableName, List<Increment>> entry : incrementMap.entrySet()) {
            TableName tableName = entry.getKey();
            List<Increment> increments = entry.getValue();
            if (logger.isDebugEnabled()) {
                logger.debug("flush {} to [{}] Increment:{}", this.getClass().getSimpleName(), tableName, increments.size());
            }
            List<List<Increment>> partition = ListUtils.partition(increments, batchSize);
            for (List<Increment> incrementList : partition) {
                asyncTemplate.increment(tableName, incrementList);
            }
        }

    }

    @Override
    public void flushAvgMax() {

        Map<RowInfo, Long> maxUpdateMap = bulkUpdater.getMaxUpdate();
        if (logger.isDebugEnabled()) {
            final int size = maxUpdateMap.size();
            if (size > 0) {
                logger.debug("flush {} checkAndMax:{}", this.getClass().getSimpleName(), size);
            }
        }

        Map<TableName, List<CheckAndMax>> maxUpdates = new HashMap<>();
        for (Map.Entry<RowInfo, Long> entry : maxUpdateMap.entrySet()) {
            final RowInfo rowInfo = entry.getKey();
            final Long val = entry.getValue();
            final byte[] rowKey = getDistributedKey(rowInfo.rowKey());
            byte[] columnName = rowInfo.columnName().getColumnName();

            CheckAndMax checkAndMax = new CheckAndMax(rowKey, family, columnName, val);

            List<CheckAndMax> checkAndMaxes = maxUpdates.computeIfAbsent(rowInfo.tableName(), k -> new ArrayList<>());
            checkAndMaxes.add(checkAndMax);
        }

        for (Map.Entry<TableName, List<CheckAndMax>> entry : maxUpdates.entrySet()) {
            TableName tableName = entry.getKey();
            List<CheckAndMax> maxs = entry.getValue();
            List<List<CheckAndMax>> partition = ListUtils.partition(maxs, batchSize);
            for (List<CheckAndMax> checkAndMaxes : partition) {
                this.asyncTemplate.maxColumnValue(tableName, checkAndMaxes);
            }
        }
    }


    private byte[] getDistributedKey(RowKey rowKey) {
        if (hasher == null) {
            return rowKey.getRowKey(ByteSaltKey.NONE.size());
        } else {
            byte[] bytes = rowKey.getRowKey(hasher.getSaltKey().size());
            return hasher.writeSaltKey(bytes);
        }
    }
}
