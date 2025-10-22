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
import com.navercorp.pinpoint.collector.applicationmap.dao.MapAgentResponseTimeDao;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.server.util.MapSlotUtils;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.hadoop.hbase.TableName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * Save response time data of WAS
 *
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 * @author HyunGil Jeong
 */
@Repository
public class HbaseMapAgentResponseTimeDao implements MapAgentResponseTimeDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseColumnFamily table;
    private final TimeSlot timeSlot;

    private final TableNameProvider tableNameProvider;
    private final BulkWriter bulkWriter;
    private final MapLinkProperties mapLinkProperties;
    private final SelfAgentNodeFactory selfAgentNodeFactory;

    public HbaseMapAgentResponseTimeDao(MapLinkProperties mapLinkProperties,
                                        HbaseColumnFamily table,
                                        TimeSlot timeSlot,
                                        TableNameProvider tableNameProvider,
                                        BulkWriter bulkWriter,
                                        SelfAgentNodeFactory selfAgentNodeFactory) {
        this.mapLinkProperties = Objects.requireNonNull(mapLinkProperties, "mapLinkConfiguration");
        this.table = Objects.requireNonNull(table, "table");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
        this.bulkWriter = Objects.requireNonNull(bulkWriter, "bulkWriter");
        this.selfAgentNodeFactory = Objects.requireNonNull(selfAgentNodeFactory, "selfAgentNodeFactory");
    }


    @Override
    public void received(long requestTime, Vertex selfVertex, String agentId, int elapsed, boolean isError) {
        Objects.requireNonNull(selfVertex, "selfVertex");

        if (logger.isDebugEnabled()) {
            logger.debug("[Self] {}/[{}]", selfVertex, agentId);
        }

        // make row key. rowkey is me
        final long rowTimeSlot = timeSlot.getTimeSlot(requestTime);
        final RowKey selfRowKey = selfAgentNodeFactory.rowkey(selfVertex, rowTimeSlot);

        final HistogramSlot slot = MapSlotUtils.getHistogramSlot(selfVertex.serviceType(), elapsed, isError);
        final ColumnName selfColumnName = selfAgentNodeFactory.histogram(agentId, slot);
        final TableName tableName = tableNameProvider.getTableName(table.getTable());
        this.bulkWriter.increment(tableName, selfRowKey, selfColumnName);

        if (mapLinkProperties.isEnableAvg()) {
            final ColumnName sumColumnName = selfAgentNodeFactory.sum(agentId, selfVertex.serviceType());
            this.bulkWriter.increment(tableName, selfRowKey, sumColumnName, elapsed);
        }

        if (mapLinkProperties.isEnableMax()) {
            final ColumnName maxColumnName = selfAgentNodeFactory.max(agentId, selfVertex.serviceType());
            this.bulkWriter.updateMax(tableName, selfRowKey, maxColumnName, elapsed);
        }
    }

    @Override
    public void updatePing(long requestTime, String applicationName, ServiceType applicationServiceType, String agentId, int elapsed, boolean isError) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(agentId, "agentId");

        if (logger.isDebugEnabled()) {
            logger.debug("[Self] {} ({})[{}]", applicationName, applicationServiceType, agentId);
        }

        // make row key. rowkey is me
        final long rowTimeSlot = timeSlot.getTimeSlot(requestTime);

        Vertex selfVertex = Vertex.of(applicationName, applicationServiceType);
        final RowKey selfRowKey = selfAgentNodeFactory.rowkey(selfVertex, rowTimeSlot);

        final HistogramSlot pingSlot = MapSlotUtils.getPingSlot(applicationServiceType);
//        final ColumnName selfColumnName = ResponseColumnName.histogram(agentId, slotNumber);

        final ColumnName selfColumnName = selfAgentNodeFactory.histogram(agentId, pingSlot);
        final TableName tableName = tableNameProvider.getTableName(table.getTable());
        this.bulkWriter.increment(tableName, selfRowKey, selfColumnName);
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
