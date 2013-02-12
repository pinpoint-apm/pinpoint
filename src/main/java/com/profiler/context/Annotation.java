package com.profiler.context;

import com.profiler.common.AnnotationNames;
import com.profiler.common.util.AnnotationTranscoder;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author netspider
 */
public class Annotation implements Thriftable {

    private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();

    //    private final long timestamp;
    private final AnnotationNames key;

    private final Object value;


    public Annotation(AnnotationNames key) {
        this.key = key;
        this.value = null;
    }

    public Annotation(AnnotationNames key, Object value) {
        this.key = key;
        this.value = value;
    }

    public AnnotationNames getKey() {
        return this.key;
    }


    @Override
    public String toString() {
        return "Annotation [key=" + key + ", value=" + value + "]";
    }

    public com.profiler.common.dto.thrift.Annotation toThrift() {
        com.profiler.common.dto.thrift.Annotation ann = new com.profiler.common.dto.thrift.Annotation();
        ann.setKey(key.getCode());

        int typeCode = transcoder.getTypeCode(value);
        byte[] encodeBytes = transcoder.encode(value, typeCode);
        ann.setValueTypeCode(typeCode);
        ann.setValue(encodeBytes);
        return ann;
    }
}
