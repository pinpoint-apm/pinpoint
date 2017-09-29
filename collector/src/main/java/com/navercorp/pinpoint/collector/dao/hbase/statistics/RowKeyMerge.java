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

package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.client.Increment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class RowKeyMerge {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final byte[] family;

    public RowKeyMerge(byte[] family) {
        if (family == null) {
            throw new NullPointerException("family must not be null");
        }
        this.family = Arrays.copyOf(family, family.length);
    }

    public  List<Increment> createBulkIncrement(Map<RowInfo, Long> data, RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        if (data.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<RowKey, List<ColumnName>> rowkeyMerge = rowKeyBaseMerge(data);

        List<Increment> incrementList = new ArrayList<>();
        for (Map.Entry<RowKey, List<ColumnName>> rowKeyEntry : rowkeyMerge.entrySet()) {
            Increment increment = createIncrement(rowKeyEntry, rowKeyDistributorByHashPrefix);
            incrementList.add(increment);
        }
        return incrementList;
    }

    private Increment createIncrement(Map.Entry<RowKey, List<ColumnName>> rowKeyEntry, RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        RowKey rowKey = rowKeyEntry.getKey();
        byte[] key = null;
        if (rowKeyDistributorByHashPrefix == null) {
            key = rowKey.getRowKey();
        } else {
            key = rowKeyDistributorByHashPrefix.getDistributedKey(rowKey.getRowKey());
        }
        final Increment increment = new Increment(key);
        for (ColumnName columnName : rowKeyEntry.getValue()) {
            increment.addColumn(family, columnName.getColumnName(), columnName.getCallCount());
        }
        logger.trace("create increment row:{}, column:{}", rowKey, rowKeyEntry.getValue());
        return increment;
    }

    private Map<RowKey, List<ColumnName>> rowKeyBaseMerge(Map<RowInfo, Long> data) {
        final Map<RowKey, List<ColumnName>> merge = new HashMap<>();

        for (Map.Entry<RowInfo, Long> entry : data.entrySet()) {
            final RowInfo rowInfo = entry.getKey();
            // write callCount to columnName and throw away
            long callCount = entry.getValue();
            rowInfo.getColumnName().setCallCount(callCount);

            RowKey rowKey = rowInfo.getRowKey();
            List<ColumnName> oldList = merge.get(rowKey);
            if (oldList == null) {
                List<ColumnName> newList = new ArrayList<>();
                newList.add(rowInfo.getColumnName());
                merge.put(rowKey, newList);
            } else {
                oldList.add(rowInfo.getColumnName());
            }
        }
        return merge;
    }
}
