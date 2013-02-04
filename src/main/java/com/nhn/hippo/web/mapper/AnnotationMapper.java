package com.nhn.hippo.web.mapper;

import com.profiler.common.bo.AnnotationBo;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.util.BytesUtils;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;

import java.util.ArrayList;
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
            byte[] buffer = kv.getBuffer();
            int spanId = BytesUtils.bytesToInt(buffer, kv.getQualifierOffset());

            int offset = kv.getValueOffset();
            if (kv.getFamilyLength() == HBaseTables.TRACES_CF_ANNOTATION.length) {
                int valueLength = kv.getValueLength();
                if (valueLength == 0) {
                    continue;
                }

                int size = BytesUtils.bytesToInt(buffer, offset);
                offset += 4;
                if (size == 0) {
                    continue;
                }

                List<AnnotationBo> bos = new ArrayList<AnnotationBo>(size);
                for (int i = 0; i < size; i++) {
                    AnnotationBo annotationBo = new AnnotationBo();
                    annotationBo.setSpanId(spanId);
                    offset = annotationBo.readValue(buffer, offset);
                    bos.add(annotationBo);
                    if (logger.isDebugEnabled()) {
                        logger.debug("read annotation:{}", annotationBo);
                    }
                }
                annotationList.put(spanId, bos);
            }
        }
        return annotationList;
    }
}
