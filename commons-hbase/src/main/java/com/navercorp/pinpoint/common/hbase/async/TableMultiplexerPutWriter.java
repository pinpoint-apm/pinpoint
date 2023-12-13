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

import com.navercorp.pinpoint.common.hbase.counter.HbaseBatchPerformanceCounter;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HTableMultiplexer;
import org.apache.hadoop.hbase.client.Put;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author Taejin Koo
 */
public class TableMultiplexerPutWriter implements HbasePutWriter {

    private final HTableMultiplexer hTableMultiplexer;

    public final HbaseBatchPerformanceCounter counter;


    public TableMultiplexerPutWriter(HTableMultiplexer hTableMultiplexer, HbaseBatchPerformanceCounter counter) {
        this.hTableMultiplexer = Objects.requireNonNull(hTableMultiplexer, "hTableMultiplexer");
        this.counter = Objects.requireNonNull(counter, "counter");
    }

    @Override
    public List<CompletableFuture<Void>> put(TableName tableName, final List<Put> puts) {
        this.counter.opsCount(puts.size());

        List<Put> results = hTableMultiplexer.put(tableName, puts);
        if (CollectionUtils.hasLength(results)) {
            this.counter.opsReject(results.size());
        }

        List<CompletableFuture<Void>> result = new ArrayList<>(puts.size());
        for (Put put : puts) {
            boolean success = isSuccess(results, put);
            CompletableFuture<Void> future = resultFuture(success);
            result.add(future);
        }

        return result;
    }

    private boolean isSuccess(List<Put> results, Put put) {
        if (CollectionUtils.isEmpty(results)) {
            return true;
        }
        return !results.contains(put);
    }


    @Override
    public CompletableFuture<Void> put(TableName tableName, Put put) {
        this.counter.opsCount();

        boolean success = this.hTableMultiplexer.put(tableName, put);
        if (!success) {
            this.counter.opsReject();
        }
        return resultFuture(success);
    }

    public <T> CompletableFuture<T> resultFuture(boolean success) {
        if (success) {
            return CompletableFuture.completedFuture(null);
        } else {
            return CompletableFuture.failedFuture(new RuntimeException("PUT failed"));
        }
    }

    @Override
    public String toString() {
        return "TableMultiplexerPutWriter{" +
                "hTableMultiplexer=" + hTableMultiplexer +
                '}';
    }
}