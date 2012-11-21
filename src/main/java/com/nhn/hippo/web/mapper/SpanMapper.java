package com.nhn.hippo.web.mapper;

import com.profiler.common.bo.AnnotationBo;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.util.BytesUtils;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        
        if(rowKey == null) {
        	return Collections.emptyList();
        }
        
        long most = BytesUtils.bytesToFirstLong(rowKey);
        long least = BytesUtils.bytesToSecondLong(rowKey);

        KeyValue[] keyList = result.raw();
        List<SpanBo> spanList = new ArrayList<SpanBo>();
        for (KeyValue kv : keyList) {
            // family name "span"일때로만 한정.
            if (kv.getFamilyLength() == HBaseTables.TRACES_CF_SPAN.length) {
                SpanBo spanBo = new SpanBo();
                spanBo.setMostTraceId(most);
                spanBo.setLeastTraceId(least);

                spanBo.setSpanID(Bytes.toLong(kv.getBuffer(), kv.getQualifierOffset()));
                spanBo.readValue(kv.getBuffer(), kv.getValueOffset());
                if (logger.isTraceEnabled()) {
                    logger.trace("read span :{}", spanBo);
                }
                spanList.add(spanBo);
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
