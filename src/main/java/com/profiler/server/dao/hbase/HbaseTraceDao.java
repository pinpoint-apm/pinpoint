package com.profiler.server.dao.hbase;

import static com.profiler.common.hbase.HBaseTables.TRACES;
import static com.profiler.common.hbase.HBaseTables.TRACES_CF_ANNOTATION;
import static com.profiler.common.hbase.HBaseTables.TRACES_CF_SPAN;
import static com.profiler.common.hbase.HBaseTables.TRACES_CF_TERMINALSPAN;

import java.util.ArrayList;
import java.util.List;

import com.profiler.common.bo.SubSpanBo;
import com.profiler.common.dto.thrift.SubSpan;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.bo.AnnotationBo;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.dto.thrift.Annotation;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.BytesUtils;
import com.profiler.common.util.SpanUtils;
import com.profiler.server.dao.TracesDao;

public class HbaseTraceDao implements TracesDao {

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Override
    public void insert(final String applicationName, final Span span) {
        SpanBo spanBo = new SpanBo(span);
        byte[] value = spanBo.writeValue();
        // TODO 서버 시간으로 변경해댜 될듯 함.
        Put put = new Put(SpanUtils.getTraceId(span), spanBo.getStartTime());
        // TODO columName이 중복일 경우를 확인가능하면 span id 중복 발급을 알수 있음.
        byte[] spanId = Bytes.toBytes(spanBo.getSpanId());
        put.add(TRACES_CF_SPAN, spanId, value);

        List<Annotation> annotations = span.getAnnotations();
        if (annotations.size() != 0) {
            byte[] bytes = wrietBuffer(annotations);
            put.add(TRACES_CF_ANNOTATION, spanId, bytes);
        }

        hbaseTemplate.put(TRACES, put);
    }

    @Override
    public void insertTerminalSpan(final String applicationName, final SubSpan span) {
        SubSpanBo spanBo = new SubSpanBo(span);
        byte[] value = spanBo.writeValue();
        // TODO 서버 시간으로 변경해댜 될듯 함. time이 생략...
        Put put = new Put(SpanUtils.getTraceId(span));

        // TODO columName이 중복일 경우를 확인가능하면 span id 중복 발급을 알수 있음.
        byte[] spanId = Bytes.toBytes(spanBo.getSpanId());
        byte[] rowId = BytesUtils.add(spanId, spanBo.getSequence());
        put.add(TRACES_CF_TERMINALSPAN, rowId, value);

        List<Annotation> annotations = span.getAnnotations();
        if (annotations.size() != 0) {
            byte[] bytes = wrietBuffer(annotations);
//			put.add(TRACES_CF_ANNOTATION, spanId, bytes);
        }

        hbaseTemplate.put(TRACES, put);
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
