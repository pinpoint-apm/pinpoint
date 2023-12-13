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

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.AsyncBufferedMutator;
import org.apache.hadoop.hbase.client.Put;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class BatchAsyncHbasePutWriter implements HbasePutWriter {
    private final AsyncTableFactory asyncTableFactory;

    public BatchAsyncHbasePutWriter(AsyncTableFactory asyncTableFactory) {
        this.asyncTableFactory = Objects.requireNonNull(asyncTableFactory, "asyncTableFactory");
    }

    @Override
    public CompletableFuture<Void> put(TableName tableName, Put put) {
        AsyncBufferedMutator buffer = asyncTableFactory.getBufferedMutator(tableName);
        return buffer.mutate(put);
    }

    @Override
    public List<CompletableFuture<Void>> put(TableName tableName, List<Put> puts) {
        AsyncBufferedMutator buffer = asyncTableFactory.getBufferedMutator(tableName);
        return buffer.mutate(puts);
    }

    @Override
    public String toString() {
        return "BatchAsyncHbasePutWriter{" +
                "asyncTableFactory=" + asyncTableFactory +
                '}';
    }
}
