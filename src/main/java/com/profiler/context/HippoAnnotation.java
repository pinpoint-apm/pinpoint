package com.profiler.context;

import com.profiler.common.util.AnnotationTranscoder;

/**
 * @author netspider
 */
public class HippoAnnotation implements Thriftable {

    private static final AnnotationTranscoder transCoder = new AnnotationTranscoder();

    private final long timestamp;
    private final String key;

    private final Object value;

    private final String threadname;

    public HippoAnnotation(long timestamp, String key) {
        this.timestamp = timestamp;
        this.key = key;
        this.value = null;
        this.threadname = Thread.currentThread().getName();
    }

    public HippoAnnotation(long timestamp, String key, Object value) {
        this.timestamp = timestamp;
        this.key = key;
        this.value = value;
        this.threadname = Thread.currentThread().getName();
    }

    public String getKey() {
        return this.key;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public String toString() {
        return "HippoAnnotation [timestamp=" + timestamp + ", key=" + key + ", value=" + value + ", threadname=" + threadname + "]";
    }

    public com.profiler.common.dto.thrift.Annotation toThrift() {
        com.profiler.common.dto.thrift.Annotation ann = new com.profiler.common.dto.thrift.Annotation();
        ann.setTimestamp(timestamp);
        ann.setKey(key);

        // TODO Encode 객체를 생성하지 않도록 변경.
        AnnotationTranscoder.Encoded encode = transCoder.encode(value);
        ann.setValueTypeCode(encode.getValueType());
        ann.setValue(encode.getBytes());
        return ann;
    }
}
