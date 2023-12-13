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
import org.apache.hadoop.hbase.client.AdvancedScanResultConsumer;
import org.apache.hadoop.hbase.client.AsyncBufferedMutator;
import org.apache.hadoop.hbase.client.AsyncBufferedMutatorBuilder;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.AsyncTable;
import org.apache.hadoop.hbase.client.AsyncTableBuilder;
import org.apache.hadoop.hbase.client.ScanResultConsumer;
import org.springframework.cache.annotation.Cacheable;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class HbaseAsyncTableFactory implements AsyncTableFactory {

    private final AsyncConnection connection;
    private final AsyncTableCustomizer customizer;

    public HbaseAsyncTableFactory(AsyncConnection connection, AsyncTableCustomizer customizer) {
        this.connection = Objects.requireNonNull(connection, "connection");
        this.customizer = Objects.requireNonNull(customizer, "customizer");
    }

    @Override
    @Cacheable(cacheNames = "AdvancedAsyncTable", keyGenerator = "tableNameAndPoolKeyGenerator", cacheManager = "hbaseAsyncTableManager")
    public AsyncTable<AdvancedScanResultConsumer> getTable(TableName tableName) {
        AsyncTableBuilder<AdvancedScanResultConsumer> builder = connection.getTableBuilder(tableName);
        this.customizer.customize(builder);
        return builder.build();
    }

    @Override
    @Cacheable(cacheNames = "AsyncTable", keyGenerator = "tableNameAndPoolKeyGenerator", cacheManager = "hbaseAsyncTableManager")
    public AsyncTable<ScanResultConsumer> getTable(TableName tableName, ExecutorService pool) {
        AsyncTableBuilder<ScanResultConsumer> builder = connection.getTableBuilder(tableName, pool);
        this.customizer.customize(builder);
        return builder.build();
    }


    @Override
    @Cacheable(cacheNames = "bufferedMutator-pool", keyGenerator = "tableNameAndPoolKeyGenerator", cacheManager = "hbaseAsyncBufferedMutatorManager")
    public AsyncBufferedMutator getBufferedMutator(TableName tableName, ExecutorService pool) {
        AsyncBufferedMutatorBuilder builder = connection.getBufferedMutatorBuilder(tableName, pool);
        customizer.customize(builder);
        return builder.build();
    }

    @Override
    @Cacheable(cacheNames = "bufferedMutator", keyGenerator = "tableNameAndPoolKeyGenerator", cacheManager = "hbaseAsyncBufferedMutatorManager")
    public AsyncBufferedMutator getBufferedMutator(TableName tableName) {
        AsyncBufferedMutatorBuilder builder = connection.getBufferedMutatorBuilder(tableName);
        customizer.customize(builder);
        return builder.build();
    }

}
