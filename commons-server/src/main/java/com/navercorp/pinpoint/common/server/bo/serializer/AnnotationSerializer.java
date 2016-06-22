package com.navercorp.pinpoint.common.server.bo.serializer;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
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
        final ByteBuffer spanId = ByteBuffer.wrap(Bytes.toBytes(spanBo.getSpanId()));

        final List<AnnotationBo> annotations = spanBo.getAnnotationBoList();
        if (CollectionUtils.isNotEmpty(annotations)) {
            ByteBuffer bytes = writeAnnotationList(annotations);
            final long acceptedTime = put.getTimeStamp();
            put.addColumn(TRACES_CF_ANNOTATION, spanId, acceptedTime, bytes);
        }

    }

    private ByteBuffer writeAnnotationList(List<AnnotationBo> annotationList) {
        final Buffer buffer = new AutomaticBuffer(64);
        return writeAnnotationList(annotationList, buffer);
    }

    // for test
    public ByteBuffer writeAnnotationList(List<AnnotationBo> annotationList, Buffer buffer) {

        if (annotationList == null) {
            annotationList = Collections.emptyList();
        }
        final int size = annotationList.size();

        buffer.putVInt(size);
        for (AnnotationBo annotationBo : annotationList) {
            writeAnnotation(annotationBo, buffer);
        }

        return buffer.wrapByteBuffer();
    }

    // for test
    public void writeAnnotation(AnnotationBo annotationBo, Buffer puffer) {
        // int key;           // required 4
        // int valueTypeCode; // required 4
        // ByteBuffer value;  // optional 4 + buf.length
        puffer.putByte(annotationBo.getRawVersion());
        puffer.putSVInt(annotationBo.getKey());
        puffer.putByte(annotationBo.getRawValueType());
        puffer.putPrefixedBytes(annotationBo.getByteValue());
    }
}
