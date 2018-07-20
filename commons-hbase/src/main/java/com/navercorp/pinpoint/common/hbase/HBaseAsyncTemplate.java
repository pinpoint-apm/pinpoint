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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.HTableMultiplexer;
import org.apache.hadoop.hbase.client.Put;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
public class HBaseAsyncTemplate implements HBaseAsyncOperation {

    private final HTableMultiplexer hTableMultiplexer;
    private final AtomicInteger opsCount = new AtomicInteger();
    private final AtomicInteger opsRejectCount = new AtomicInteger();

    public HBaseAsyncTemplate(Configuration conf, int perRegionServerBufferQueueSize) throws IOException {
        this.hTableMultiplexer = new HTableMultiplexer(conf, perRegionServerBufferQueueSize);
    }

    public HBaseAsyncTemplate(Connection connection, Configuration conf, int perRegionServerBufferQueueSize) throws IOException {
        this.hTableMultiplexer = new HTableMultiplexer(connection, conf, perRegionServerBufferQueueSize);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean put(TableName tableName, Put put) {
        opsCount.incrementAndGet();

        boolean success = hTableMultiplexer.put(tableName, put);
        if (!success) {
            opsRejectCount.incrementAndGet();
        }
        return success;
    }

    @Override
    public List<Put> put(TableName tableName, List<Put> puts) {
        opsCount.addAndGet(puts.size());

        List<Put> rejectPuts = hTableMultiplexer.put(tableName, puts);
        if (rejectPuts != null && rejectPuts.size() > 0) {
            opsRejectCount.addAndGet(rejectPuts.size());
        }
        return rejectPuts;
    }

    @Override
    public Long getOpsCount() {
        return opsCount.longValue();
    }

    @Override
    public Long getOpsRejectedCount() {
        return opsRejectCount.longValue();
    }

    @Override
    public Long getCurrentOpsCount() {
        return hTableMultiplexer.getHTableMultiplexerStatus().getTotalBufferedCounter();
    }

    @Override
    public Long getOpsFailedCount() {
        return hTableMultiplexer.getHTableMultiplexerStatus().getTotalFailedCounter();
    }

    @Override
    public Long getOpsAverageLatency() {
        return hTableMultiplexer.getHTableMultiplexerStatus().getOverallAverageLatency();
    }

    @Override
    public Map<String, Long> getCurrentOpsCountForEachRegionServer() {
        return hTableMultiplexer.getHTableMultiplexerStatus().getBufferedCounterForEachRegionServer();
    }

    @Override
    public Map<String, Long> getOpsFailedCountForEachRegionServer() {
        return hTableMultiplexer.getHTableMultiplexerStatus().getFailedCounterForEachRegionServer();
    }

    @Override
    public Map<String, Long> getOpsAverageLatencyForEachRegionServer() {
        return hTableMultiplexer.getHTableMultiplexerStatus().getAverageLatencyForEachRegionServer();
    }

}