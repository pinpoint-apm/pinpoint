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

        // TODO Encode 객체를 생성하지 않도록 변경.
        AnnotationTranscoder.Encoded encode = transcoder.encode(value);
        ann.setValueTypeCode(encode.getValueType());
        ann.setValue(encode.getBytes());
        return ann;
    }
}
