package com.navercorp.pinpoint.common.server.bo.serializer.trace.v1;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.util.AnnotationTranscoder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AnnotationBoDecoder {

    private final AnnotationTranscoder transcoder = new AnnotationTranscoder();
    private static final byte VERSION = AnnotationSerializer.VERSION;


    public List<AnnotationBo> decode(Buffer qualifier, Buffer valueBuffer, SpanDecodingContext decodingContext) {

        long spanId = qualifier.readLong();
        decodingContext.setSpanId(spanId);

        return decode(valueBuffer);
    }

    // for test
    List<AnnotationBo> decode(Buffer valueBuffer) {
        final int annotationSize = valueBuffer.readVInt();
        if (annotationSize == 0) {
            // don' fix return Collections.emptyList();
            // exist outer add method
            return new ArrayList<AnnotationBo>();
        }

        List<AnnotationBo> annotationBoList = new ArrayList<AnnotationBo>(annotationSize);
        for (int i = 0; i < annotationSize; i++) {

            AnnotationBo annotation = decodeAnnotation(valueBuffer);
            annotationBoList.add(annotation);

        }

        return annotationBoList;
    }


    private AnnotationBo decodeAnnotation(Buffer buffer) {

        final AnnotationBo annotation = new AnnotationBo();

        final byte version = buffer.readByte();
        if (version != VERSION) {
            throw new IllegalStateException("unknown version:" + version);
        }
        annotation.setKey(buffer.readSVInt());

        byte valueType = buffer.readByte();
        byte[] byteValue = buffer.readPrefixedBytes();

        Object decodeObject = transcoder.decode(valueType, byteValue);
        annotation.setValue(decodeObject);

        return annotation;
    }
}
