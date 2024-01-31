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

import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.hadoop.hbase.TableName;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class HbaseAsyncCacheConfiguration {

    public HbaseAsyncCacheConfiguration() {
    }

    @Bean
    public KeyGenerator tableNameAndPoolKeyGenerator() {
        return new TableNameAndPoolKeyGenerator();
    }

    private static class TableNameAndPoolKeyGenerator implements KeyGenerator {

        @Override
        public Object generate(Object target, Method method, Object... params) {
            final int length = params.length;
            if (length == 1) {
                TableName tableName = (TableName) params[0];
                return new TableNameAndPoolKey(tableName);
            } else if (length == 2) {
                TableName tableName = (TableName) params[0];
                ExecutorService executorService = (ExecutorService) params[1];
                return new TableNameAndPoolKey(tableName, executorService);
            }
            throw new IllegalArgumentException("invalid params");
        }

        private static class TableNameAndPoolKey {
            private final TableName tableName;
            private final ExecutorService pool;

            public TableNameAndPoolKey(TableName tableName) {
                this.tableName = Objects.requireNonNull(tableName, "tableName");
                this.pool = null;
            }

            public TableNameAndPoolKey(TableName tableName, ExecutorService pool) {
                this.tableName = Objects.requireNonNull(tableName, "tableName");
                this.pool = Objects.requireNonNull(pool, "pool");
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                TableNameAndPoolKey that = (TableNameAndPoolKey) o;

                if (!tableName.equals(that.tableName)) return false;
                return Objects.equals(pool, that.pool);
            }

            @Override
            public int hashCode() {
                int result = tableName.hashCode();
                result = 31 * result + (pool != null ? pool.hashCode() : 0);
                return result;
            }

            @Override
            public String toString() {
                return "TableNameAndPoolKey{" +
                        "tableName=" + tableName +
                        ", pool=" + pool +
                        '}';
            }
        }
    }


    @Bean
    public CacheManager hbaseAsyncTableManager() {
        Caffeine<Object, Object> builder = newCacheBuilder();

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(builder);
        return cacheManager;
    }

    @Bean
    public CacheManager hbaseAsyncBufferedMutatorManager() {
        Caffeine<Object, Object> builder = newCacheBuilder();

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(builder);
        return cacheManager;
    }

    private Caffeine<Object, Object> newCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .initialCapacity(200)
                .maximumSize(1000);
    }

}
