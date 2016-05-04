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

import com.navercorp.pinpoint.collector.dao.TracesDao;
import com.navercorp.pinpoint.collector.dao.hbase.filter.SpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.serializer.AnnotationSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.SpanEventSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.SpanSerializer;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import static com.navercorp.pinpoint.common.hbase.HBaseTables.*;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.util.SpanUtils;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
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
public class HbaseTraceDao implements TracesDao {

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
    @Qualifier("traceDistributor")
    private AbstractRowKeyDistributor rowKeyDistributor;

    @Override
    public void insert(final TSpan span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }

        final SpanBo spanBo = new SpanBo(span);

        final byte[] rowKey = getDistributeRowKey(SpanUtils.getTransactionId(span));
        final Put put = new Put(rowKey);

        this.spanSerializer.serialize(spanBo, put, null);
        this.annotationSerializer.serialize(spanBo, put, null);


        addNestedSpanEvent(put, span);

        boolean success = hbaseTemplate.asyncPut(TRACES, put);
        if (!success) {
            hbaseTemplate.put(TRACES, put);
        }
    }

    private byte[] getDistributeRowKey(byte[] transactionId) {
        return rowKeyDistributor.getDistributedKey(transactionId);
    }

    private void addNestedSpanEvent(Put put, TSpan span) {
        final List<TSpanEvent> spanEventBoList = span.getSpanEventList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return;
        }


        for (TSpanEvent spanEvent : spanEventBoList) {
            final SpanEventBo spanEventBo = new SpanEventBo(span, spanEvent);
            addColumn(put, spanEventBo);
        }
    }

    @Override
    public void insertSpanChunk(TSpanChunk spanChunk) {
        final byte[] rowKey = getDistributeRowKey(SpanUtils.getTransactionId(spanChunk));
        final Put put = new Put(rowKey);

        final List<TSpanEvent> spanEventBoList = spanChunk.getSpanEventList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return;
        }


        for (TSpanEvent spanEvent : spanEventBoList) {
            final SpanEventBo spanEventBo = new SpanEventBo(spanChunk, spanEvent);
            addColumn(put, spanEventBo);
        }

        if (!put.isEmpty()) {
            boolean success = hbaseTemplate.asyncPut(TRACES, put);
            if (!success) {
                hbaseTemplate.put(TRACES, put);
            }
        }
    }

    private void addColumn(Put put, SpanEventBo spanEventBo) {
        if (!spanEventFilter.filter(spanEventBo)) {
            return;
        }
        this.spanEventSerializer.serialize(spanEventBo, put, null);
    }


}
