/*
 * Copyright 2025 NAVER Corp.
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
 */

package com.navercorp.pinpoint.common.hbase.config;

import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.HbaseTableV2;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.AsyncTableRegionLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AsyncWarmup implements Consumer<AsyncConnection> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final TableNameProvider tableNameProvider;

    private List<HbaseTable> warmUpExclusive;

    public AsyncWarmup(TableNameProvider tableNameProvider) {
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    public void setWarmUpExclusive(List<HbaseTable> warmUpExclusive) {
        this.warmUpExclusive = warmUpExclusive;
    }

    public void asyncWarmup(AsyncConnection connection) {
        String warmup = this.getClass().getSimpleName();

        logger.info("{} for hbase AsyncConnection started", warmup);
        List<HbaseTable> warmUpInclusive = new ArrayList<>(List.of(HbaseTableV2.values()));
        if (warmUpExclusive != null) {
            warmUpInclusive.removeAll(warmUpExclusive);
        }

        for (HbaseTable hBaseTable : warmUpInclusive) {
            try {
                TableName tableName = tableNameProvider.getTableName(hBaseTable);
                logger.info("{} for hbase table start: {}", warmup, tableName.toString());

                StopWatch stopWatch = new StopWatch(warmup + "-" + tableName.getNameAsString());
                stopWatch.start();

                AsyncTableRegionLocator regionLocator = connection.getRegionLocator(tableName);
                CompletableFuture<List<HRegionLocation>> allRegionFuture = regionLocator.getAllRegionLocations();
                List<HRegionLocation> allRegion = allRegionFuture.get(3, TimeUnit.MINUTES);

                stopWatch.stop();
                logger.info("{} allRegionLocations {}  regionSize:{} {}ms", warmup, tableName, allRegion.size(), stopWatch.getTotalTimeMillis());
            } catch (Throwable th) {
                logger.warn("Failed to {} for Table:{}. message:{}", warmup, hBaseTable.getName(), th.getMessage(), th);
            }
        }
    }

    @Override
    public void accept(AsyncConnection connection) {
        asyncWarmup(connection);
    }
}
