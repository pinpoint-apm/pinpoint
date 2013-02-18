package com.profiler.context;

import com.profiler.common.AnnotationKey;
import com.profiler.common.util.AnnotationTranscoder;

/**
 * @author netspider
 */
public class Annotation implements Thriftable {

    private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();

    //    private final long timestamp;
    private final AnnotationKey key;

    private final Object value;


    public Annotation(AnnotationKey key) {
        this.key = key;
        this.value = null;
    }

    public Annotation(AnnotationKey key, Object value) {
        this.key = key;
        this.value = value;
    }

    public AnnotationKey getKey() {
        return this.key;
    }


    @Override
    public String toString() {
        return "Annotation [key=" + key + ", value=" + value + "]";
    }

    public com.profiler.common.dto.thrift.Annotation toThrift() {
        com.profiler.common.dto.thrift.Annotation ann = new com.profiler.common.dto.thrift.Annotation();
        ann.setKey(key.getCode());
        transcoder.mappingValue(value, ann);
        return ann;
    }
}
