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

import com.navercorp.pinpoint.collector.dao.TraceDao;

import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.filter.SpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v1.AnnotationSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v1.SpanEventEncodingContext;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v1.SpanEventSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v1.SpanSerializer;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import static com.navercorp.pinpoint.common.hbase.HBaseTables.*;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.util.TransactionId;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author emeroad
 */
@Repository
public class HbaseTraceDao implements TraceDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AcceptedTimeService acceptedTimeService;

    @Autowired
    private SpanEventFilter spanEventFilter;

    @Autowired
    private SpanSerializer spanSerializer;

    @Autowired
    private SpanEventSerializer spanEventSerializer;

    @Autowired
    private AnnotationSerializer annotationSerializer;

    @Autowired
    @Qualifier("traceRowKeyEncoderV1")
    private RowKeyEncoder<TransactionId> rowKeyEncoder;

    @Override
    public void insert(final SpanBo spanBo) {
        if (spanBo == null) {
            throw new NullPointerException("span must not be null");
        }


        long acceptedTime = spanBo.getCollectorAcceptTime();

        TransactionId transactionId = spanBo.getTransactionId();
        final byte[] rowKey = rowKeyEncoder.encodeRowKey(transactionId);
        final Put put = new Put(rowKey, acceptedTime);

        this.spanSerializer.serialize(spanBo, put, null);
        this.annotationSerializer.serialize(spanBo, put, null);


        addNestedSpanEvent(put, spanBo);

        boolean success = hbaseTemplate.asyncPut(TRACES, put);
        if (!success) {
            hbaseTemplate.put(TRACES, put);
        }
    }

    private void addNestedSpanEvent(Put put, SpanBo span) {
        final List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return;
        }


        for (SpanEventBo spanEvent : spanEventBoList) {
            addColumn(put, span, spanEvent);
        }
    }

    @Override
    public void insertSpanChunk(SpanChunkBo spanChunkBo) {
        TransactionId transactionId = spanChunkBo.getTransactionId();
        final byte[] rowKey = rowKeyEncoder.encodeRowKey(transactionId);
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        final Put put = new Put(rowKey, acceptedTime);

        final List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return;
        }


        for (SpanEventBo spanEventBo : spanEventBoList) {
            addColumn(put, spanChunkBo, spanEventBo);
        }

        if (!put.isEmpty()) {
            boolean success = hbaseTemplate.asyncPut(TRACES, put);
            if (!success) {
                hbaseTemplate.put(TRACES, put);
            }
        }
    }

    private void addColumn(Put put, BasicSpan basicSpan, SpanEventBo spanEventBo) {
        if (!spanEventFilter.filter(spanEventBo)) {
            return;
        }
        SpanEventEncodingContext spanEventEncodingContext = new SpanEventEncodingContext(basicSpan, spanEventBo);
        this.spanEventSerializer.serialize(spanEventEncodingContext, put, null);
    }


}
