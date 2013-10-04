package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.thrift.dto.TAnnotation;
import com.nhn.pinpoint.profiler.util.AnnotationTranscoder;

/**
 * @author netspider
 */
public class TraceAnnotation implements Thriftable {

    private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();

    private final AnnotationKey key;
    private final Object value;

    public TraceAnnotation(AnnotationKey key) {
        this.key = key;
        this.value = null;
    }

    public TraceAnnotation(AnnotationKey key, Object value) {
        this.key = key;
        this.value = value;
    }

    public AnnotationKey getAnnotationKey() {
        return this.key;
    }


    @Override
    public String toString() {
        return "Annotation [key=" + key + ", value=" + value + "]";
    }

    public TAnnotation toThrift() {
        TAnnotation ann = new TAnnotation();
        ann.setKey(key.getCode());
        transcoder.mappingValue(value, ann);
        return ann;
    }
}
