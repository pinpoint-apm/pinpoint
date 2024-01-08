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

import com.navercorp.pinpoint.common.hbase.HbaseSystemException;
import com.navercorp.pinpoint.common.hbase.counter.HbaseBatchPerformanceCounter;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class HbasePutCounter implements HbasePutWriter {

    private final HbasePutWriter writer;

    private final HbaseBatchPerformanceCounter counter;

    private final BiConsumer<Void, Throwable> resultStatus;

    public HbasePutCounter(HbasePutWriter writer, HbaseBatchPerformanceCounter counter) {
        this.writer = Objects.requireNonNull(writer, "writer");
        this.counter = Objects.requireNonNull(counter, "counter");
        this.resultStatus = new BiConsumer<>() {
            @Override
            public void accept(Void unused, Throwable throwable) {
                if (throwable != null) {
                    counter.opsFailed();
                } else {
                    counter.success();
                }
            }
        };

    }

    @Override
    public CompletableFuture<Void> put(TableName tableName, Put put) {
        counter.opsCount();

        try {
            CompletableFuture<Void> future = this.writer.put(tableName, put);
            future.whenComplete(resultStatus);
            return future;
        } catch (Throwable throwable) {
            this.counter.opsReject();
            if (throwable instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new HbaseSystemException(throwable);
        }
    }

    @Override
    public List<CompletableFuture<Void>> put(TableName tableName, List<Put> puts) {
        counter.opsCount();

        try {
            List<CompletableFuture<Void>> futures = this.writer.put(tableName, puts);
            for (CompletableFuture<Void> future : futures) {
                future.whenComplete(resultStatus);
            }
            return futures;
        } catch (Throwable throwable) {
            this.counter.opsReject(puts.size());
            if (throwable instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new HbaseSystemException(throwable);
        }

    }
}
