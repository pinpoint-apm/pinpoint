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

package com.navercorp.pinpoint.collector.applicationmap.dao.hbase;

import com.navercorp.pinpoint.collector.applicationmap.config.MapLinkProperties;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import org.apache.hadoop.hbase.TableName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * Update statistics of caller node
 * 
 * @author netspider
 * @author emeroad
 * @author HyunGil Jeong
 */
@Repository
public class HbaseMapOutLinkDao implements MapOutLinkDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseColumnFamily table;

    private final TableNameProvider tableNameProvider;
    private final BulkWriter bulkWriter;
    private final MapLinkProperties mapLinkProperties;

    private final OutLinkFactory outLinkFactory;

    public HbaseMapOutLinkDao(MapLinkProperties mapLinkProperties,
                              HbaseColumnFamily table,
                              TableNameProvider tableNameProvider,
                              BulkWriter bulkWriter,
                              OutLinkFactory outLinkFactory) {
        this.mapLinkProperties = Objects.requireNonNull(mapLinkProperties, "mapLinkConfiguration");
        this.table = Objects.requireNonNull(table, "table");

        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.bulkWriter = Objects.requireNonNull(bulkWriter, "bulkWrtier");
        this.outLinkFactory = Objects.requireNonNull(outLinkFactory, "outLinkFactory");
    }


    @Override
    public void outLink(long requestTime, Vertex selfVertex, String selfAgentId,
                        Vertex outVertex, String outHost, int elapsed, boolean isError) {
        Objects.requireNonNull(selfVertex, "selfVertex");
        Objects.requireNonNull(outVertex, "inVertex");

        if (logger.isDebugEnabled()) {
            logger.debug("[OutLink] {}/{} -> {}/{}", selfVertex, selfAgentId, outVertex, outHost);
        }

        // there may be no endpoint in case of httpclient
        outHost = Objects.toString(outHost, "");

        // make row key. rowkey is me
        OutLinkFactory.OutLink outLink = outLinkFactory.newOutLink(selfVertex.applicationName(), selfVertex.serviceType(), selfAgentId,
                outVertex.applicationName(), outVertex.serviceType(), outHost);

        final RowKey selfLinkRowKey = outLink.rowkey(requestTime);

        final ColumnName inLink = outLink.histogram(elapsed, isError);
        final TableName tableName = tableNameProvider.getTableName(table.getTable());
        this.bulkWriter.increment(tableName, selfLinkRowKey, inLink);

        if (mapLinkProperties.isEnableAvg()) {
            final ColumnName sumInLink = outLink.sum();
            this.bulkWriter.increment(tableName, selfLinkRowKey, sumInLink, elapsed);
        }
        if (mapLinkProperties.isEnableMax()) {
            final ColumnName maxInLink = outLink.max();
            this.bulkWriter.updateMax(tableName, selfLinkRowKey, maxInLink, elapsed);
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