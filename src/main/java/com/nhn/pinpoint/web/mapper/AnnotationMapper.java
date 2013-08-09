package com.nhn.pinpoint.web.mapper;

import com.nhn.pinpoint.common.bo.AnnotationBo;
import com.nhn.pinpoint.common.bo.AnnotationBoList;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class AnnotationMapper implements RowMapper<Map<Integer, List<AnnotationBo>>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<Integer, List<AnnotationBo>> mapRow(Result result, int rowNum) throws Exception {
        KeyValue[] keyList = result.raw();
        Map<Integer, List<AnnotationBo>> annotationList = new HashMap<Integer, List<AnnotationBo>>();

        for (KeyValue kv : keyList) {
            final byte[] bytes = kv.getBuffer();
            Buffer buffer = new FixedBuffer(bytes, kv.getQualifierOffset());
            int spanId = buffer.readInt();
            if (Bytes.equals(kv.getFamily(), HBaseTables.TRACES_CF_ANNOTATION)) {
                int valueLength = kv.getValueLength();
                if (valueLength == 0) {
                    continue;
                }

                buffer.setOffset(kv.getValueOffset());
                AnnotationBoList annotationBoList = new AnnotationBoList();
                annotationBoList.readValue(buffer);
                if (annotationBoList.size() > 0 ) {
                    annotationBoList.setSpanId(spanId);
                    annotationList.put(spanId, annotationBoList.getAnnotationBoList());
                }
            }
        }
        return annotationList;
    }
}
