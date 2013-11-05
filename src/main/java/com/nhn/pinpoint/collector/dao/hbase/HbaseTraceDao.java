package com.nhn.pinpoint.collector.dao.hbase;

import com.nhn.pinpoint.common.bo.AnnotationBo;
import com.nhn.pinpoint.common.bo.AnnotationBoList;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;
import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.thrift.dto.TAnnotation;
import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.thrift.dto.TSpanChunk;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.SpanUtils;
import com.nhn.pinpoint.collector.dao.TracesDao;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.nhn.pinpoint.common.hbase.HBaseTables.*;

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
    @Qualifier("rowKeyDistributor")
    private AbstractRowKeyDistributor rowKeyDistributor;

    @Override
    public void insert(final TSpan span) {

        SpanBo spanBo = new SpanBo(span);
        final byte[] rowKey = getDistributeRowKey(SpanUtils.getTransactionId(span));
        Put put = new Put(rowKey);

        byte[] spanValue = spanBo.writeValue();
        // TODO columName이 중복일 경우를 확인가능하면 span id 중복 발급을 알수 있음.
        byte[] spanId = Bytes.toBytes(spanBo.getSpanId());

        long acceptedTime = acceptedTimeService.getAcceptedTime();
        put.add(TRACES_CF_SPAN, spanId, acceptedTime, spanValue);

        List<TAnnotation> annotations = span.getAnnotations();
        if (CollectionUtils.isNotEmpty(annotations)) {
            byte[] bytes = writeAnnotation(annotations);
            put.add(TRACES_CF_ANNOTATION, spanId, bytes);
        }

        addNestedSpanEvent(put, span);

        hbaseTemplate.put(TRACES, put);

    }

    private byte[] getDistributeRowKey(byte[] transactionId) {
        return rowKeyDistributor.getDistributedKey(transactionId);
    }

    private void addNestedSpanEvent(Put put, TSpan span) {
        List<TSpanEvent> spanEventBoList = span.getSpanEventList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return;
        }

        long acceptedTime0 = acceptedTimeService.getAcceptedTime();
        for (TSpanEvent spanEvent : spanEventBoList) {
            SpanEventBo spanEventBo = new SpanEventBo(span, spanEvent);
            byte[] rowId = BytesUtils.add(spanEventBo.getSpanId(), spanEventBo.getSequence());
            byte[] value = spanEventBo.writeValue();
            put.add(TRACES_CF_TERMINALSPAN, rowId, acceptedTime0, value);
        }
    }



    @Override
    public void insertSpanChunk(TSpanChunk spanChunk) {
        byte[] rowKey = getDistributeRowKey(SpanUtils.getTransactionId(spanChunk));
        Put put = new Put(rowKey);

        long acceptedTime = acceptedTimeService.getAcceptedTime();
        List<TSpanEvent> spanEventBoList = spanChunk.getSpanEventList();
        for (TSpanEvent spanEvent : spanEventBoList) {
            SpanEventBo spanEventBo = new SpanEventBo(spanChunk, spanEvent);

            byte[] value = spanEventBo.writeValue();
            byte[] rowId = BytesUtils.add(spanEventBo.getSpanId(), spanEventBo.getSequence());

            put.add(TRACES_CF_TERMINALSPAN, rowId, acceptedTime, value);
        }
        hbaseTemplate.put(TRACES, put);

    }

    private byte[] writeAnnotation(List<TAnnotation> annotations) {
        List<AnnotationBo> boList = new ArrayList<AnnotationBo>(annotations.size());
        for (TAnnotation ano : annotations) {
            AnnotationBo annotationBo = new AnnotationBo(ano);
            boList.add(annotationBo);
        }

        Buffer buffer = new AutomaticBuffer(64);
        AnnotationBoList annotationBoList = new AnnotationBoList(boList);
        annotationBoList.writeValue(buffer);
        return buffer.getBuffer();
    }
}
