/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.scatter.dao.hbase;

import com.navercorp.pinpoint.collector.scatter.dao.TraceIndexDao;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.HbasePutWriter;
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

@Repository
public class HbaseTraceIndexDao implements TraceIndexDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseColumnFamily indexTable = HbaseTables.TRACE_INDEX;
    private final HbaseColumnFamily metaTable = HbaseTables.TRACE_INDEX_META;

    private final HbasePutWriter putWriter;
    private final TableNameProvider tableNameProvider;

    private final RowKeyEncoder<SpanBo> traceIndexRowKeyEncoder;

    public HbaseTraceIndexDao(HbasePutWriter putWriter,
                              TableNameProvider tableNameProvider,
                              @Qualifier("traceIndexRowKeyEncoder") RowKeyEncoder<SpanBo> traceIndexRowKeyEncoder) {
        this.putWriter = Objects.requireNonNull(putWriter, "putWriter");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.traceIndexRowKeyEncoder = Objects.requireNonNull(traceIndexRowKeyEncoder, "traceIndexRowKeyEncoder");
        logger.info("traceIndexRowKeyEncoder:{}", traceIndexRowKeyEncoder);
    }

    @Override
    public void insert(final SpanBo span) {
        Objects.requireNonNull(span, "span");

        if (logger.isDebugEnabled()) {
            logger.debug("insert TraceIndex: {}", span);
        }

        final long acceptedTime = span.getCollectorAcceptTime();
        final byte[] distributedKey = traceIndexRowKeyEncoder.encodeRowKey(span);

        final Put put = new Put(distributedKey, true);

        final byte[] indexValue = buildIndexValue(span);
        put.addColumn(indexTable.getName(), indexTable.getName(), acceptedTime, indexValue);

        final byte[] metaDataValue = buildMetaValue(span);
        put.addColumn(metaTable.getName(), metaTable.getName(), acceptedTime, metaDataValue);

        final TableName applicationTraceIndexTableName = tableNameProvider.getTableName(indexTable.getTable());
        putWriter.put(applicationTraceIndexTableName, put);
    }

    /**
     * DotMetaData.BuilderV2.readIndex();
     */
    private byte[] buildIndexValue(SpanBo span) {
        final Buffer buffer = new AutomaticBuffer(10 + HbaseTableConstants.AGENT_ID_MAX_LEN);
        byte hasError = span.getErrCode() == 0 ? (byte) 0 : (byte) 1;
        buffer.putByte(hasError);
        buffer.putPrefixedString(span.getAgentId());
        buffer.putVInt(span.getElapsed());
        buffer.putSVInt(span.getErrCode());
        return buffer.getBuffer();
    }

    /**
     * DotMetaData.BuilderV2.readMeta();
     */
    private byte[] buildMetaValue(SpanBo span) {
        Buffer buffer = new AutomaticBuffer(64);
        buffer.putInt(span.getElapsed());
        buffer.putByte((byte) 1); // txId version
        SpanUtils.writeTransactionIdV1(buffer, span.getTransactionId());

        buffer.putLong(span.getStartTime());
        buffer.putPrefixedString(span.getRpc());
        buffer.putPrefixedString(span.getRemoteAddr());
        buffer.putPrefixedString(span.getEndPoint());
        buffer.putPrefixedString(span.getAgentName());
        return buffer.getBuffer();
    }

}