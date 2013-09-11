package com.nhn.pinpoint.collector.dao.hbase;

import com.nhn.pinpoint.common.bo.AnnotationBo;
import com.nhn.pinpoint.common.bo.AnnotationBoList;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;
import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.thrift.dto.Annotation;
import com.nhn.pinpoint.thrift.dto.Span;
import com.nhn.pinpoint.thrift.dto.SpanChunk;
import com.nhn.pinpoint.thrift.dto.SpanEvent;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.SpanUtils;
import com.nhn.pinpoint.collector.dao.TracesDao;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.nhn.pinpoint.common.hbase.HBaseTables.*;

@Repository
public class HbaseTraceDao implements TracesDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AcceptedTimeService acceptedTimeService;

    @Autowired
    private AbstractRowKeyDistributor rowKeyDistributor;

    @Override
    public void insert(final Span span) {

        SpanBo spanBo = new SpanBo(span);
        final byte[] rowKey = getDistributeRowKey(SpanUtils.getTransactionId(span));
        Put put = new Put(rowKey);

        byte[] spanValue = spanBo.writeValue();
        // TODO columName이 중복일 경우를 확인가능하면 span id 중복 발급을 알수 있음.
        byte[] spanId = Bytes.toBytes(spanBo.getSpanId());

        long acceptedTime = acceptedTimeService.getAcceptedTime();
        put.add(TRACES_CF_SPAN, spanId, acceptedTime, spanValue);

        List<Annotation> annotations = span.getAnnotations();
        if (annotations != null && annotations.size() != 0) {
            byte[] bytes = writeAnnotation(annotations);
            put.add(TRACES_CF_ANNOTATION, spanId, bytes);
        }

        addNestedSpanEvent(put, span);

        hbaseTemplate.put(TRACES, put);

    }

    private byte[] getDistributeRowKey(byte[] transactionId) {
        return rowKeyDistributor.getDistributedKey(transactionId);
    }

    private void addNestedSpanEvent(Put put, Span span) {
        List<SpanEvent> spanEventBoList = span.getSpanEventList();
        if (spanEventBoList == null || spanEventBoList.size() == 0) {
            return;
        }

        long acceptedTime0 = acceptedTimeService.getAcceptedTime();
        for (SpanEvent spanEvent : spanEventBoList) {
            SpanEventBo spanEventBo = new SpanEventBo(span, spanEvent);
            byte[] rowId = BytesUtils.add(spanEventBo.getSpanId(), spanEventBo.getSequence());
            byte[] value = spanEventBo.writeValue();
            put.add(TRACES_CF_TERMINALSPAN, rowId, acceptedTime0, value);
        }
    }


    @Override
    public void insertEvent(final SpanEvent spanEvent) {
        SpanEventBo spanEventBo = new SpanEventBo(spanEvent);
        byte[] value = spanEventBo.writeValue();

        final byte[] rowKey = getDistributeRowKey(SpanUtils.getTransactionId(spanEvent));
        Put spanEventPut = new Put(rowKey);

        byte[] rowId = BytesUtils.add(spanEventBo.getSpanId(), spanEventBo.getSequence());
        spanEventPut.add(TRACES_CF_TERMINALSPAN, rowId, value);

        hbaseTemplate.put(TRACES, spanEventPut);
    }

    @Override
    public void insertSpanChunk(SpanChunk spanChunk) {
        byte[] rowKey = getDistributeRowKey(SpanUtils.getTransactionId(spanChunk));
        Put put = new Put(rowKey);

        long acceptedTime = acceptedTimeService.getAcceptedTime();
        List<SpanEvent> spanEventBoList = spanChunk.getSpanEventList();
        for (SpanEvent spanEvent : spanEventBoList) {
            SpanEventBo spanEventBo = new SpanEventBo(spanChunk, spanEvent);

            byte[] value = spanEventBo.writeValue();
            byte[] rowId = BytesUtils.add(spanEventBo.getSpanId(), spanEventBo.getSequence());

            put.add(TRACES_CF_TERMINALSPAN, rowId, acceptedTime, value);
        }
        hbaseTemplate.put(TRACES, put);

    }

    private byte[] writeAnnotation(List<Annotation> annotations) {
        List<AnnotationBo> boList = new ArrayList<AnnotationBo>(annotations.size());
        for (Annotation ano : annotations) {
            AnnotationBo annotationBo = new AnnotationBo(ano);
            boList.add(annotationBo);
        }

        Buffer buffer = new AutomaticBuffer(64);
        AnnotationBoList annotationBoList = new AnnotationBoList(boList);
        annotationBoList.writeValue(buffer);
        return buffer.getBuffer();
    }
}
