/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.config.ScatterConfiguration;
import com.navercorp.pinpoint.collector.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.ApplicationNameRowKeyEncoder;
import com.navercorp.pinpoint.common.server.scatter.FuzzyRowKeyFactory;
import com.navercorp.pinpoint.common.server.scatter.OneByteFuzzyRowKeyFactory;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.util.SpanUtils;

import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;


/**
 * find traceids by application name
 * 
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseApplicationTraceIndexDao implements ApplicationTraceIndexDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily.ApplicationTraceIndexTrace DESCRIPTOR = HbaseColumnFamily.APPLICATION_TRACE_INDEX_TRACE;

    private final HbaseOperations2 hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    private final AcceptedTimeService acceptedTimeService;

    private final AbstractRowKeyDistributor rowKeyDistributor;

    private final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();
    private final ScatterConfiguration scatterConfiguration;

    private final ApplicationNameRowKeyEncoder rowKeyEncoder = new ApplicationNameRowKeyEncoder();

    public HbaseApplicationTraceIndexDao(HbaseOperations2 hbaseTemplate,
                                         TableNameProvider tableNameProvider,
                                         @Qualifier("applicationTraceIndexDistributor") AbstractRowKeyDistributor rowKeyDistributor,
                                         AcceptedTimeService acceptedTimeService,
                                         ScatterConfiguration scatterConfiguration) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.scatterConfiguration = Objects.requireNonNull(scatterConfiguration, "scatterConfiguration");
    }

    @Override
    public void insert(final SpanBo span) {
        Objects.requireNonNull(span, "span");

        if (logger.isDebugEnabled()) {
            logger.debug("insert ApplicationTraceIndex: {}", span);
        }

        // Assert agentId
        CollectorUtils.checkAgentId(span.getAgentId());
        // Assert applicationName
        CollectorUtils.checkApplicationName(span.getApplicationId());

        final Buffer buffer = new AutomaticBuffer(10 + HbaseTableConstants.AGENT_ID_MAX_LEN);
        buffer.putVInt(span.getElapsed());
        buffer.putSVInt(span.getErrCode());
        buffer.putPrefixedString(span.getAgentId());
        final byte[] value = buffer.getBuffer();

        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        final byte[] distributedKey = createRowKey(span, acceptedTime);

        final Put put = new Put(distributedKey);

        put.addColumn(DESCRIPTOR.getName(), makeQualifier(span) , acceptedTime, value);

        final TableName applicationTraceIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.asyncPut(applicationTraceIndexTableName, put);
    }

    private byte[] makeQualifier(final SpanBo span) {
        final byte[] qualifier = SpanUtils.getVarTransactionId(span);
        return qualifier;
    }

    private byte[] createRowKey(SpanBo span, long acceptedTime) {
        if (scatterConfiguration.getServerSideScan() == ScatterConfiguration.ServerSideScan.v2) {
            return createRowKeyV2(span, acceptedTime);
        }
        return createRowKeyV1(span, acceptedTime);
    }

    private byte[] createRowKeyV1(SpanBo span, long acceptedTime) {
        // distribute key evenly
        final byte[] applicationTraceIndexRowKey = rowKeyEncoder.encodeRowKey(span.getApplicationId(), acceptedTime);
        return rowKeyDistributor.getDistributedKey(applicationTraceIndexRowKey);
    }


    private byte[] createRowKeyV2(SpanBo span, long acceptedTime) {
        // distribute key evenly
        byte fuzzyKey = fuzzyRowKeyFactory.getKey(span.getElapsed());
        final byte[] appTraceIndexRowKey = newRowKeyV2(span.getApplicationId(), acceptedTime, fuzzyKey);
        return rowKeyDistributor.getDistributedKey(appTraceIndexRowKey);
    }

    byte[] newRowKeyV2(String applicationName, long acceptedTime, byte fuzzySlotKey) {
        Objects.requireNonNull(applicationName, "applicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("fuzzySlotKey:{}", fuzzySlotKey);
        }
        byte[] rowKey = rowKeyEncoder.encodeRowKey(applicationName, acceptedTime);

        byte[] fuzzyRowKey = new byte[rowKey.length + 1];
        System.arraycopy(rowKey, 0, fuzzyRowKey, 0, rowKey.length);

        fuzzyRowKey[rowKey.length] = fuzzySlotKey;
        return fuzzyRowKey;
    }
}