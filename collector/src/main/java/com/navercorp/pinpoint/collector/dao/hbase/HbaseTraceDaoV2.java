/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.TraceDao;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanChunkSerializerV2;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanSerializerV2;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;

import com.navercorp.pinpoint.common.util.Assert;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
@Repository
public class HbaseTraceDaoV2 implements TraceDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseOperations2 hbaseTemplate;

    private final TableDescriptor<HbaseColumnFamily.Trace> descriptor;

    private final SpanSerializerV2 spanSerializer;

    private final SpanChunkSerializerV2 spanChunkSerializer;

    private final RowKeyEncoder<TransactionId> rowKeyEncoder;

    public HbaseTraceDaoV2(@Qualifier("asyncPutHbaseTemplate") HbaseOperations2 hbaseTemplate,
                           TableDescriptor<HbaseColumnFamily.Trace> descriptor,
                           @Qualifier("traceRowKeyEncoderV2") RowKeyEncoder<TransactionId> rowKeyEncoder,
                           SpanSerializerV2 spanSerializer,
                           SpanChunkSerializerV2 spanChunkSerializer) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.spanSerializer = Objects.requireNonNull(spanSerializer, "spanSerializer");
        this.spanChunkSerializer = Assert.requireNonNull(spanChunkSerializer, "spanChunkSerializer");
    }

    @Override
    public boolean insert(final SpanBo spanBo) {
        Objects.requireNonNull(spanBo, "spanBo");
        if (logger.isDebugEnabled()) {
            logger.debug("insert trace: {}", spanBo);
        }

        // Assert agentId
        CollectorUtils.checkAgentId(spanBo.getAgentId());
        // Assert applicationName
        CollectorUtils.checkApplicationName(spanBo.getApplicationId());

        long acceptedTime = spanBo.getCollectorAcceptTime();

        TransactionId transactionId = spanBo.getTransactionId();
        final byte[] rowKey = this.rowKeyEncoder.encodeRowKey(transactionId);
        final Put put = new Put(rowKey, acceptedTime);

        this.spanSerializer.serialize(spanBo, put, null);

        TableName traceTableName = descriptor.getTableName();

        return hbaseTemplate.asyncPut(traceTableName, put);
    }

    @Override
    public boolean insertSpanChunk(SpanChunkBo spanChunkBo) {
        Objects.requireNonNull(spanChunkBo, "spanChunkBo");

        TransactionId transactionId = spanChunkBo.getTransactionId();
        final byte[] rowKey = this.rowKeyEncoder.encodeRowKey(transactionId);

        final long acceptedTime = spanChunkBo.getCollectorAcceptTime();
        final Put put = new Put(rowKey, acceptedTime);

        final List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return true;
        }

        this.spanChunkSerializer.serialize(spanChunkBo, put, null);

        if (!put.isEmpty()) {
            TableName traceTableName = descriptor.getTableName();
            return hbaseTemplate.asyncPut(traceTableName, put);
        }

        return false;
    }
}
