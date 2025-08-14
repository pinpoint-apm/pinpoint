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
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Warmup implements Consumer<Connection> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final TableNameProvider tableNameProvider;

    private List<HbaseTable> warmUpExclusive;

    public Warmup(TableNameProvider tableNameProvider) {
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    public void setWarmUpExclusive(List<HbaseTable> warmUpExclusive) {
        this.warmUpExclusive = warmUpExclusive;
    }

    public void warmup(Connection connection) {
        String warmup = this.getClass().getSimpleName();

        logger.info("{} for hbase Connection started", warmup);
        List<HbaseTable> warmUpInclusive = new ArrayList<>(List.of(HbaseTableV2.values()));
        if (warmUpExclusive != null) {
            warmUpInclusive.removeAll(warmUpExclusive);
        }

        for (HbaseTable hBaseTable : warmUpInclusive) {
            try {
                TableName tableName = tableNameProvider.getTableName(hBaseTable);
                logger.info("{} for hbase table start: {}", warmup, tableName.toString());

                StopWatch stopWatch = new StopWatch(this.getClass().getName() + "-" + tableName.getNameAsString());
                stopWatch.start();

                List<HRegionLocation> allRegions = getAllRegions(connection, tableName);

                stopWatch.stop();
                logger.info("{} allRegionLocations {}  regionSize:{} {}ms", warmup, tableName, allRegions.size(), stopWatch.getTotalTimeMillis());
            } catch (IOException e) {
                logger.warn("Failed to {} for Table:{}. message:{}", warmup, hBaseTable.getName(), e.getMessage(), e);
            }
        }
    }

    private List<HRegionLocation> getAllRegions(Connection connection, TableName tableName) throws IOException {
        try (RegionLocator regionLocator = connection.getRegionLocator(tableName)) {
            return regionLocator.getAllRegionLocations();
        }
    }

    @Override
    public void accept(Connection connection) {
        warmup(connection);
    }
}
