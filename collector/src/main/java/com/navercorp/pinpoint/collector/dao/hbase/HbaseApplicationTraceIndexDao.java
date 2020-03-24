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

import com.navercorp.pinpoint.collector.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstatns;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.util.SpanUtils;

import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseOperations2 hbaseTemplate;

    private final TableDescriptor<HbaseColumnFamily.ApplicationTraceIndexTrace> descriptor;

    private final AcceptedTimeService acceptedTimeService;

    private final AbstractRowKeyDistributor rowKeyDistributor;

    public HbaseApplicationTraceIndexDao(@Qualifier("asyncPutHbaseTemplate") HbaseOperations2 hbaseTemplate,
                                         TableDescriptor<HbaseColumnFamily.ApplicationTraceIndexTrace> descriptor,
                                         @Qualifier("applicationTraceIndexDistributor") AbstractRowKeyDistributor rowKeyDistributor,
                                         AcceptedTimeService acceptedTimeService) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
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

        final Buffer buffer = new AutomaticBuffer(10 + HbaseTableConstatns.AGENT_NAME_MAX_LEN);
        buffer.putVInt(span.getElapsed());
        buffer.putSVInt(span.getErrCode());
        buffer.putPrefixedString(span.getAgentId());
        final byte[] value = buffer.getBuffer();

        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        final byte[] distributedKey = createRowKey(span, acceptedTime);
        final Put put = new Put(distributedKey);

        put.addColumn(descriptor.getColumnFamilyName(), makeQualifier(span) , acceptedTime, value);

        final TableName applicationTraceIndexTableName = descriptor.getTableName();
        hbaseTemplate.asyncPut(applicationTraceIndexTableName, put);
    }

    private byte[] makeQualifier(final SpanBo span) {
        final byte[] qualifier = SpanUtils.getVarTransactionId(span);
        return qualifier;
    }

    private byte[] createRowKey(SpanBo span, long acceptedTime) {
        // distribute key evenly
        final byte[] applicationTraceIndexRowKey = SpanUtils.getApplicationTraceIndexRowKey(span.getApplicationId(), acceptedTime);
        return rowKeyDistributor.getDistributedKey(applicationTraceIndexRowKey);
    }
}