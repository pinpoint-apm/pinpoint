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

package com.navercorp.pinpoint.collector.applicationmap.dao.v3;

import com.navercorp.pinpoint.collector.applicationmap.config.MapLinkProperties;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapApplicationResponseDao;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.dao.CachedStatisticsDao;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import org.apache.hadoop.hbase.TableName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class HbaseMapApplicationResponseDao implements MapApplicationResponseDao, CachedStatisticsDao {


    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseColumnFamily table;

    private final TableNameProvider tableNameProvider;
    private final BulkWriter bulkWriter;
    private final MapLinkProperties mapLinkProperties;
    private final SelfAppNodeFactory selfAppNodeFactory;

    public HbaseMapApplicationResponseDao(MapLinkProperties mapLinkProperties,
                                          HbaseColumnFamily table,
                                          TableNameProvider tableNameProvider,
                                          BulkWriter bulkWriter,
                                          SelfAppNodeFactory selfAppNodeFactory) {
        this.mapLinkProperties = Objects.requireNonNull(mapLinkProperties, "mapLinkConfiguration");
        this.table = Objects.requireNonNull(table, "table");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.bulkWriter = Objects.requireNonNull(bulkWriter, "bulkWriter");
        this.selfAppNodeFactory = Objects.requireNonNull(selfAppNodeFactory, "selfAppNodeFactory");
    }


    @Override
    public void received(long requestTime, Vertex selfVertex, int elapsed, boolean isError) {
        Objects.requireNonNull(selfVertex, "selfVertex");

        if (logger.isDebugEnabled()) {
            logger.debug("[SelfApp] {}", selfVertex);
        }

        // make row key. rowkey is me
        SelfAppNodeFactory.Node node = selfAppNodeFactory.newNode(selfVertex.applicationName(), selfVertex.serviceType());
        final RowKey selfRowKey = node.rowkey(requestTime);

        final ColumnName selfColumnName = node.histogram(elapsed, isError);
        final TableName tableName = tableNameProvider.getTableName(table.getTable());
        this.bulkWriter.increment(tableName, selfRowKey, selfColumnName);

        if (mapLinkProperties.isEnableAvg()) {
            final ColumnName sumColumnName = node.sum();
            this.bulkWriter.increment(tableName, selfRowKey, sumColumnName, elapsed);
        }

        if (mapLinkProperties.isEnableMax()) {
            final ColumnName maxColumnName = node.max();
            this.bulkWriter.updateMax(tableName, selfRowKey, maxColumnName, elapsed);
        }
    }

    @Override
    public void flushLink() {
        this.bulkWriter.flushLink();
    }

    @Override
    public void flushAvgMax() {
        this.bulkWriter.flushAvgMax();
    }

}
