package com.profiler.context;

import com.profiler.common.util.AnnotationTranscoder;

/**
 * @author netspider
 */
public class HippoAnnotation implements Thriftable {

    private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();

    //    private final long timestamp;
    private final String key;

    private final Object value;

    private final String threadname;

    public HippoAnnotation(String key) {
        this.key = key;
        this.value = null;
        this.threadname = Thread.currentThread().getName();
    }

    public HippoAnnotation(String key, Object value) {
        this.key = key;
        this.value = value;
        this.threadname = Thread.currentThread().getName();
    }

    public String getKey() {
        return this.key;
    }


    @Override
    public String toString() {
        return "HippoAnnotation [key=" + key + ", value=" + value + ", threadname=" + threadname + "]";
    }

    public com.profiler.common.dto.thrift.Annotation toThrift() {
        com.profiler.common.dto.thrift.Annotation ann = new com.profiler.common.dto.thrift.Annotation();
        ann.setKey(key);

        int typeCode = transcoder.getTypeCode(value);
        byte[] encodeBytes = transcoder.encode(value, typeCode);
        ann.setValueTypeCode(typeCode);
        ann.setValue(encodeBytes);
        return ann;
    }
}
