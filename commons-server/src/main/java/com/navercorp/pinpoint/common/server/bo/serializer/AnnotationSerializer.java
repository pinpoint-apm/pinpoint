package com.navercorp.pinpoint.common.server.bo.serializer;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.TRACES_CF_ANNOTATION;

/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class AnnotationSerializer implements HbaseSerializer<SpanBo, Put> {


    @Override
    public void serialize(SpanBo spanBo, Put put, SerializationContext context) {

        // TODO  if we can identify whether the columnName is duplicated or not,
        // we can also know whether the span id is duplicated or not.
        final byte[] spanId = Bytes.toBytes(spanBo.getSpanId());

        final List<AnnotationBo> annotations = spanBo.getAnnotationBoList();
        if (CollectionUtils.isNotEmpty(annotations)) {
            byte[] bytes = writeAnnotationList(annotations);
            put.addColumn(TRACES_CF_ANNOTATION, spanId, bytes);
        }

    }

    private byte[] writeAnnotationList(List<AnnotationBo> annotationList) {
        final Buffer buffer = new AutomaticBuffer(64);
        return writeAnnotationList(annotationList, buffer);
    }

    // for test
    public byte[] writeAnnotationList(List<AnnotationBo> annotationList, Buffer buffer) {

        if (annotationList == null) {
            annotationList = Collections.emptyList();
        }
        final int size = annotationList.size();

        buffer.putVar(size);
        for (AnnotationBo annotationBo : annotationList) {
            writeAnnotation(annotationBo, buffer);
        }

        return buffer.getBuffer();
    }

    // for test
    public void writeAnnotation(AnnotationBo annotationBo, Buffer puffer) {
        // int key;           // required 4
        // int valueTypeCode; // required 4
        // ByteBuffer value;  // optional 4 + buf.length
        puffer.put(annotationBo.getRawVersion());
        puffer.putSVar(annotationBo.getKey());
        puffer.put(annotationBo.getRawValueType());
        puffer.putPrefixedBytes(annotationBo.getByteValue());
    }
}
