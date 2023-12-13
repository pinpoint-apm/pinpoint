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
import org.apache.hadoop.hbase.TableName;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.BiConsumer;

public class ResultLoggingListener<R> implements BiConsumer<R, Throwable> {
    private final Logger logger;
    private final TableName tableName;
    private final MutationType mutationType;

    public ResultLoggingListener(Logger logger, TableName tableName, MutationType mutationType) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.mutationType = Objects.requireNonNull(mutationType, "mutationType");
    }

    @Override
    public void accept(R v, Throwable throwable) {
        if (throwable != null) {
            logger.warn("{} {} failed", tableName, mutationType, throwable);
        } else {
            logger.trace("{} {} success", tableName, mutationType);
        }
    }
}