package com.profiler.server.dao;

import com.profiler.common.bo.AnnotationBo;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.dto.thrift.Annotation;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.BytesUtils;
import com.profiler.common.util.SpanUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class HbaseTraceDao implements TraceDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private final byte[] COLFAM_SPAN = HBaseTables.TRACES_CF_SPAN;
    private final byte[] COLFAM_ANNOTATION = HBaseTables.TRACES_CF_ANNOTATION;

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Override
    public void insert(final Span span) {

        SpanBo spanBo = new SpanBo(span);
        byte[] value = spanBo.writeValue();

        Put put = new Put(SpanUtils.getTraceId(span), spanBo.getTimestamp());
        // TODO columName이 중복일 경우를 확인가능하면 span id 중복 발급을 알수 있음.
        byte[] spanId = Bytes.toBytes(spanBo.getSpanId());
        put.add(COLFAM_SPAN, spanId, value);

        List<Annotation> annotations = span.getAnnotations();
        if (annotations.size() != 0) {
            byte[] bytes = wrietBuffer(annotations);
            put.add(COLFAM_ANNOTATION, spanId, bytes);
        }

        hbaseTemplate.put(HBaseTables.TRACES, put);
    }

    private byte[] wrietBuffer(List<Annotation> annotations) {
        int size = 0;
        List<AnnotationBo> boList = new ArrayList<AnnotationBo>(annotations.size());
        for (Annotation ano : annotations) {
            AnnotationBo annotationBo = new AnnotationBo(ano);
            size += annotationBo.getBufferSize();
            boList.add(annotationBo);
        }
        // size
        size += 4;

        byte[] buffer = new byte[size];
        int offset = 0;
        BytesUtils.writeInt(boList.size(), buffer, offset);
        offset = 4;
        for (AnnotationBo bo : boList) {
            offset = bo.writeValue(buffer, offset);
        }
        return buffer;
    }


}
