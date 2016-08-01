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
            return new ArrayList<>();
        }

        List<AnnotationBo> annotationBoList = new ArrayList<>(annotationSize);
        for (int i = 0; i < annotationSize; i++) {

            AnnotationBo annotation = decodeAnnotation(valueBuffer);
            annotationBoList.add(annotation);

        }

        return annotationBoList;
    }


    private AnnotationBo decodeAnnotation(Buffer buffer) {

        final AnnotationBo annotation = new AnnotationBo();

        annotation.setVersion(buffer.readByte());
        annotation.setKey(buffer.readSVInt());

        byte valueType = buffer.readByte();
        annotation.setValueType(valueType);

        byte[] byteValue = buffer.readPrefixedBytes();
        annotation.setByteValue(byteValue);

        Object decodeObject = transcoder.decode(valueType, byteValue);
        annotation.setValue(decodeObject);

        return annotation;
    }
}
