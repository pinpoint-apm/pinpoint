package com.navercorp.pinpoint.common.hbase.async;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.AsyncBufferedMutator;
import org.apache.hadoop.hbase.client.AsyncBufferedMutatorBuilder;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.springframework.cache.annotation.Cacheable;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class HbaseAsyncBufferedMutatorFactory implements AsyncBufferedMutatorFactory {

    private final AsyncConnection connection;
    private final AsyncBufferedMutatorCustomizer customizer;

    public HbaseAsyncBufferedMutatorFactory(AsyncConnection connection, AsyncBufferedMutatorCustomizer customizer) {
        this.connection = Objects.requireNonNull(connection, "connection");
        this.customizer = Objects.requireNonNull(customizer, "customizer");
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
