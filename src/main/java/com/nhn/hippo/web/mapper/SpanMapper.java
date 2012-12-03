package com.nhn.hippo.web.mapper;

import com.profiler.common.bo.AnnotationBo;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SubSpanBo;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.util.BytesUtils;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 *
 */
@Component
public class SpanMapper implements RowMapper<List<SpanBo>> {

    private final byte[] COLFAM_SPAN = Bytes.toBytes("Span");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AnnotationMapper annotationMapper;

    public AnnotationMapper getAnnotationMapper() {
        return annotationMapper;
    }

    public void setAnnotationMapper(AnnotationMapper annotationMapper) {
        this.annotationMapper = annotationMapper;
    }

    @Override
    public List<SpanBo> mapRow(Result result, int rowNum) throws Exception {
        byte[] rowKey = result.getRow();

        if (rowKey == null) {
            return Collections.emptyList();
        }

        long most = BytesUtils.bytesToFirstLong(rowKey);
        long least = BytesUtils.bytesToSecondLong(rowKey);

        KeyValue[] keyList = result.raw();
        List<SpanBo> spanList = new ArrayList<SpanBo>();
        Map<Long, SpanBo> spanMap = new HashMap<Long, SpanBo>();
        List<SubSpanBo> subSpanBoList = new ArrayList<SubSpanBo>();
        for (KeyValue kv : keyList) {
            // family name "span"일때로만 한정.
            if (kv.getFamilyLength() == HBaseTables.TRACES_CF_SPAN.length) {
                SpanBo spanBo = new SpanBo();
                spanBo.setMostTraceId(most);
                spanBo.setLeastTraceId(least);

                spanBo.setSpanID(Bytes.toLong(kv.getBuffer(), kv.getQualifierOffset()));
                spanBo.readValue(kv.getBuffer(), kv.getValueOffset());
                if (logger.isDebugEnabled()) {
                    logger.debug("read span :{}", spanBo);
                }
                spanList.add(spanBo);
                spanMap.put(spanBo.getSpanId(), spanBo);
            } else if (kv.getFamilyLength() == HBaseTables.TRACES_CF_TERMINALSPAN.length) {
                SubSpanBo subSpanBo = new SubSpanBo();
                subSpanBo.setMostTraceId(most);
                subSpanBo.setLeastTraceId(least);

                long spanId = Bytes.toLong(kv.getBuffer(), kv.getQualifierOffset());
                short sequence = Bytes.toShort(kv.getBuffer(), kv.getQualifierOffset() + 8);
                subSpanBo.setSpanId(spanId);
                subSpanBo.setSequence(sequence);

                subSpanBo.readValue(kv.getBuffer(), kv.getValueOffset());
                if (logger.isDebugEnabled()) {
                    logger.debug("read subSpan :{}", subSpanBo);
                }
                subSpanBoList.add(subSpanBo);
            }
        }
        for (SubSpanBo subSpanBo : subSpanBoList) {
            SpanBo spanBo = spanMap.get(subSpanBo.getSpanId());
            if (spanBo != null) {
                spanBo.addSubSpan(subSpanBo);
            }
        }
        if (annotationMapper != null) {
            Map<Long, List<AnnotationBo>> annotationMap = annotationMapper.mapRow(result, rowNum);
            addAnnotation(spanList, annotationMap);
        }


        return spanList;

    }

    private void addAnnotation(List<SpanBo> spanList, Map<Long, List<AnnotationBo>> annotationMap) {
        for (SpanBo bo : spanList) {
            long spanID = bo.getSpanId();
            List<AnnotationBo> anoList = annotationMap.get(spanID);
            bo.setAnnotationBoList(anoList);
        }
    }
}
