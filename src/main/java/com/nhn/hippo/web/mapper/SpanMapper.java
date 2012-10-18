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
                spanBo.setSpanID(Bytes.toLong(kv.getQualifier()));
                spanBo.setTimestamp(kv.getTimestamp());
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

//        NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(COLFAM_SPAN);
//        if (familyMap == null) {
//            return Collections.emptyList();
//        }

//        List<SpanBo> spanList = new ArrayList<SpanBo>(familyMap.size());
//        Put put = new Put(SpanUtils.getTracesRowkey(span), span.getTimestamp());
//        // TODO columName이 중복일 경우를 확인가능하면 span id 중복 발급을 알수 있음.
//        put.add(COLFAM_SPAN, Bytes.toBytes(span.getSpanID()), value);

//        byte[] rowKey = result.getRow();
//        long most = BytesUtils.bytesToFirstLong(rowKey);
//        long least = BytesUtils.bytesToSecondLong(rowKey);
//
//        for (NavigableMap.Entry<byte[], byte[]> entry : familyMap.entrySet()) {
//            SpanBo spanBo = new SpanBo();
//
//            spanBo.setMostTraceID(most);
//            spanBo.setLeastTraceID(least);
//            spanBo.setSpanID(Bytes.toLong(entry.getKey()));
//            //
//            //byte[] spanId = entry.getKey();
////            if (binaryAnnotationDecoder != null) {
////                binaryAnnotationDecoder.decode(span);
////            }
//
//            if (logger.isDebugEnabled()) {
//                logger.debug("read span :{}", spanBo);
//            }
//            spanList.add(spanBo);
//        }
//        return spanList;
    }

    private void addAnnotation(List<SpanBo> spanList, Map<Long, List<AnnotationBo>> annotationMap) {
        for (SpanBo bo : spanList) {
            long spanID = bo.getSpanId();
            List<AnnotationBo> anoList = annotationMap.get(spanID);
            bo.setAnnotationBoList(anoList);
        }
    }
}
