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

package com.navercorp.pinpoint.common.hbase.config;

import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Warmup implements Consumer<Connection> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final TableNameProvider tableNameProvider;

    private List<HbaseTable> warmUpExclusive = List.of(HbaseTable.AGENT_URI_STAT);

    public Warmup(TableNameProvider tableNameProvider) {
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    public void setWarmUpExclusive(List<HbaseTable> warmUpExclusive) {
        this.warmUpExclusive = warmUpExclusive;
    }

    public void warmup(Connection connection) {

        logger.info("warmup for hbase connection started");
        List<HbaseTable> warmUpInclusive = new ArrayList<>(List.of(HbaseTable.values()));
        if (warmUpExclusive != null) {
            warmUpInclusive.removeAll(warmUpExclusive);
        }

        for (HbaseTable hBaseTable : warmUpInclusive) {
            try {
                TableName tableName = tableNameProvider.getTableName(hBaseTable);
                logger.info("warmup for hbase table start: {}", tableName.toString());
                RegionLocator regionLocator = connection.getRegionLocator(tableName);
                regionLocator.getAllRegionLocations();
            } catch (IOException e) {
                logger.warn("Failed to warmup for Table:{}. message:{}", hBaseTable.getName(), e.getMessage(), e);
            }
        }
    }

    @Override
    public void accept(Connection connection) {
        warmup(connection);
    }
}
