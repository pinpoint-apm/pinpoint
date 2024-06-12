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
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.HbasePutWriter;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.util.SpanUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final HbaseColumnFamily.ApplicationTraceIndexTrace INDEX = HbaseColumnFamily.APPLICATION_TRACE_INDEX_TRACE_VER2;
    private static final HbaseColumnFamily.ApplicationTraceIndexTrace META = HbaseColumnFamily.APPLICATION_TRACE_INDEX_META_VER2;

    private final HbasePutWriter putWriter;
    private final TableNameProvider tableNameProvider;
    private final RowKeyEncoder<SpanBo> applicationIndexRowKeyEncoder;

    public HbaseApplicationTraceIndexDao(HbasePutWriter putWriter,
                                         TableNameProvider tableNameProvider,
                                         @Qualifier("applicationIndexRowKeyEncoder") RowKeyEncoder<SpanBo> applicationIndexRowKeyEncoder) {
        this.putWriter = Objects.requireNonNull(putWriter, "putWriter");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.applicationIndexRowKeyEncoder = Objects.requireNonNull(applicationIndexRowKeyEncoder, "applicationIndexRowKeyEncoder");
        logger.info("ApplicationIndexRowKeyEncoder:{}", applicationIndexRowKeyEncoder);
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
        CollectorUtils.checkApplicationName(span.getApplicationName());

        final long acceptedTime = span.getCollectorAcceptTime();
        final byte[] distributedKey = applicationIndexRowKeyEncoder.encodeRowKey(span);

        final Put put = new Put(distributedKey);

        final byte[] qualifier = SpanUtils.getVarTransactionId(span);

        final byte[] indexValue = buildIndexValue(span);
        put.addColumn(INDEX.getName(), qualifier, acceptedTime, indexValue);

        final byte[] metaDataValue = buildMetaData(span);
        put.addColumn(META.getName(), qualifier, metaDataValue);

        final TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        putWriter.put(applicationTraceIndexTableName, put);
    }

    private byte[] buildIndexValue(SpanBo span) {
        final Buffer buffer = new AutomaticBuffer(10 + HbaseTableConstants.AGENT_ID_MAX_LEN);
        buffer.putVInt(span.getElapsed());
        buffer.putSVInt(span.getErrCode());
        buffer.putPrefixedString(AgentId.unwrap(span.getAgentId()));
        return buffer.getBuffer();
    }

    /**
     * DotMetaData.Builder.read();
     */
    private byte[] buildMetaData(SpanBo span) {
        Buffer buffer = new AutomaticBuffer(64);
        buffer.putByte((byte) 0);
        buffer.putLong(span.getSpanId());
        buffer.putLong(span.getStartTime());
        // fixed field offset
        buffer.setByte(0, (byte) buffer.getOffset());

        buffer.putPrefixedString(span.getRpc());
        buffer.putPrefixedString(span.getRemoteAddr());
        buffer.putPrefixedString(span.getEndPoint());
        buffer.putPrefixedString(span.getAgentName());

        return buffer.getBuffer();
    }

}