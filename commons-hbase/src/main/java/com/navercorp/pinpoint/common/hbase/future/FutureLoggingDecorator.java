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

package com.navercorp.pinpoint.common.hbase.future;

import com.navercorp.pinpoint.common.hbase.util.MutationType;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class FutureLoggingDecorator implements FutureDecorator {

    private final Logger logger;

    public FutureLoggingDecorator(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public <R> void apply(CompletableFuture<R> future, TableName tableName, MutationType mutationType) {
        if (future == null) {
            return;
        }

        final BiConsumer<R, Throwable> listener = new ResultLoggingListener<>(logger, tableName, mutationType);
        future.whenComplete(listener);
    }

    public <R> void apply(List<CompletableFuture<R>> futures, TableName tableName, MutationType mutationType) {
        if (CollectionUtils.isEmpty(futures)) {
            return;
        }

        final BiConsumer<R, Throwable> listener = new ResultLoggingListener<>(logger, tableName, mutationType);
        for (CompletableFuture<R> future : futures) {
            future.whenComplete(listener);
        }
    }
}
