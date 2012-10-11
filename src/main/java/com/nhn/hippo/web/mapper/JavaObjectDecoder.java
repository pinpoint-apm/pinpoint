package com.nhn.hippo.web.mapper;

import com.profiler.common.dto.thrift.Annotation;
import com.profiler.common.dto.thrift.BinaryAnnotation;
import com.profiler.common.dto.thrift.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 임시 객체
 */
public class JavaObjectDecoder implements BinaryAnnotationDecoder {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void decode(Span span) {
        List<Annotation> annotations = span.getAnnotations();

        List<BinaryAnnotation> binaryAnnotations = span.getBinaryAnnotations();
        for (BinaryAnnotation binaryAnnotation : binaryAnnotations) {
            Object decode = decode(binaryAnnotation);
            Annotation annotation = new Annotation(binaryAnnotation.getTimestamp(), binaryAnnotation.getKey() + ":" + decode.toString());
            annotations.add(annotation);
        }
    }


    private Object decode(BinaryAnnotation binaryAnnotation) {
        ByteArrayInputStream ins = new ByteArrayInputStream(binaryAnnotation.getValue());
        try {
            ObjectInputStream in = new ObjectInputStream(ins);
            Object readObject = in.readObject();
            return readObject;
        } catch (IOException e) {
            logger.warn("binaryAnnotation decode fail Cause:{}", e.getMessage(), e);
            return "binaryAnnotation decode fail Cause:" + e.getMessage();
        } catch (ClassNotFoundException e) {
            logger.warn("binaryAnnotation decode fail Cause:{}", e.getMessage(), e);
            return "binaryAnnotation decode fail Cause:" + e.getMessage();
        }
    }
}
