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

import com.navercorp.pinpoint.common.hbase.future.FutureDecorator;
import com.navercorp.pinpoint.common.hbase.future.FutureLoggingDecorator;
import com.navercorp.pinpoint.common.hbase.util.MutationType;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class LoggingHbasePutWriter implements HbasePutWriter {
    private final HbasePutWriter delegate;
    private final FutureDecorator decorator;

    public LoggingHbasePutWriter(HbasePutWriter delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");

        Logger logger = LogManager.getLogger(delegate.getClass());
        this.decorator = new FutureLoggingDecorator(logger);
    }

    @Override
    public CompletableFuture<Void> put(TableName tableName, Put put) {
        CompletableFuture<Void> future = delegate.put(tableName, put);
        decorator.apply(future, tableName, MutationType.PUT);
        return future;
    }

    @Override
    public List<CompletableFuture<Void>> put(TableName tableName, List<Put> puts) {
        List<CompletableFuture<Void>> futures = delegate.put(tableName, puts);
        decorator.apply(futures, tableName, MutationType.PUT);
        return futures;
    }


}
