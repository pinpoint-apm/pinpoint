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
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.HbasePutWriter;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanChunkSerializerV2;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanSerializerV2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Woonduk Kang(emeroad)
 */
@Repository
public class HbaseTraceDaoV2 implements TraceDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily.Trace descriptor = HbaseColumnFamily.TRACE_V2_SPAN;

    private final TableNameProvider tableNameProvider;

    private final SpanSerializerV2 spanSerializer;

    private final SpanChunkSerializerV2 spanChunkSerializer;

    private final RowKeyEncoder<TransactionId> rowKeyEncoder;
    private final HbasePutWriter putWriter;

    public HbaseTraceDaoV2(@Qualifier("spanPutWriter")
                           HbasePutWriter putWriter,
                           TableNameProvider tableNameProvider,
                           @Qualifier("traceRowKeyEncoderV2") RowKeyEncoder<TransactionId> rowKeyEncoder,
                           SpanSerializerV2 spanSerializer,
                           SpanChunkSerializerV2 spanChunkSerializer) {
        this.putWriter = Objects.requireNonNull(putWriter, "putWriter");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.spanSerializer = Objects.requireNonNull(spanSerializer, "spanSerializer");
        this.spanChunkSerializer = Objects.requireNonNull(spanChunkSerializer, "spanChunkSerializer");
    }

    @Override
    public boolean insert(SpanBo span) {
        CompletableFuture<Void> future = asyncInsert(span);
        try {
            future.get(3, TimeUnit.SECONDS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("insert interrupted", e);
            return false;
        } catch (ExecutionException | TimeoutException e) {
            logger.debug("insert failed", e);
            return false;
        }
    }

    @Override
    public CompletableFuture<Void> asyncInsert(final SpanBo spanBo) {
        Objects.requireNonNull(spanBo, "spanBo");
        if (logger.isDebugEnabled()) {
            logger.debug("insert trace: {}", spanBo);
        }

        // Assert agentId
        CollectorUtils.checkAgentId(spanBo.getAgentId());
        // Assert applicationName
        CollectorUtils.checkApplicationName(spanBo.getApplicationName());

        long acceptedTime = spanBo.getCollectorAcceptTime();

        TransactionId transactionId = spanBo.getTransactionId();
        final byte[] rowKey = this.rowKeyEncoder.encodeRowKey(transactionId);
        final Put put = new Put(rowKey, acceptedTime);

        this.spanSerializer.serialize(spanBo, put, null);

        TableName traceTableName = tableNameProvider.getTableName(descriptor.getTable());
        return putWriter.put(traceTableName, put);
    }

    @Override
    public void insertSpanChunk(SpanChunkBo spanChunkBo) {
        Objects.requireNonNull(spanChunkBo, "spanChunkBo");

        TransactionId transactionId = spanChunkBo.getTransactionId();
        final byte[] rowKey = this.rowKeyEncoder.encodeRowKey(transactionId);

        final long acceptedTime = spanChunkBo.getCollectorAcceptTime();
        final Put put = new Put(rowKey, acceptedTime);

        final List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return;
        }

        this.spanChunkSerializer.serialize(spanChunkBo, put, null);

        if (put.isEmpty()) {
            return;
        }
        TableName traceTableName = tableNameProvider.getTableName(descriptor.getTable());
        this.putWriter.put(traceTableName, put);
    }
}
