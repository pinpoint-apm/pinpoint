package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.util.AnnotationTranscoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AnnotationBoDecoder {

    private final AnnotationTranscoder transcoder = new AnnotationTranscoder();


    public List<AnnotationBo> decode(Buffer buffer) {

        final int size = buffer.readVInt();
        if (size == 0) {
            // don' fix return Collections.emptyList();
            // exist outer add method
            return new ArrayList<>();
        }

        List<AnnotationBo> annotationBoList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {

            AnnotationBo annotation = decodeAnnotation(buffer);
            annotationBoList.add(annotation);

        }

        return annotationBoList;
    }


    public AnnotationBo decodeAnnotation(Buffer buffer) {

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
