/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class DisabledHBaseAsyncOperation implements HBaseAsyncOperation {

    static final DisabledHBaseAsyncOperation INSTANCE = new DisabledHBaseAsyncOperation();

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public boolean put(TableName tableName, Put put) {
        return false;
    }

    @Override
    public List<Put> put(TableName tableName, List<Put> puts) {
        return puts;
    }

    @Override
    public Long getOpsCount() {
        return -1L;
    }

    @Override
    public Long getOpsRejectedCount() {
        return -1L;
    }

    @Override
    public Long getCurrentOpsCount() {
        return -1L;
    }

    @Override
    public Long getOpsFailedCount() {
        return -1L;
    }

    @Override
    public Long getOpsAverageLatency() {
        return -1L;
    }

    @Override
    public Map<String, Long> getCurrentOpsCountForEachRegionServer() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Long> getOpsFailedCountForEachRegionServer() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Long> getOpsAverageLatencyForEachRegionServer() {
        return Collections.emptyMap();
    }

}