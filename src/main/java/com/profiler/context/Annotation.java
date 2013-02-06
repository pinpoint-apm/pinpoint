package com.profiler.context;

import com.profiler.common.AnnotationNames;
import com.profiler.common.util.AnnotationTranscoder;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author netspider
 */
public class Annotation implements Thriftable {
    private static final Logger logger = Logger.getLogger(Annotation.class.getName());
    private boolean isDebug = logger.isLoggable(Level.FINE);

    private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();

    //    private final long timestamp;
    private final AnnotationNames key;

    private final Object value;

    private String threadname;

    public Annotation(AnnotationNames key) {
        this.key = key;
        this.value = null;
        if (isDebug) {
            this.threadname = Thread.currentThread().getName();
        }
    }

    public Annotation(AnnotationNames key, Object value) {
        this.key = key;
        this.value = value;
        if (isDebug) {
            this.threadname = Thread.currentThread().getName();
        }
    }

    public AnnotationNames getKey() {
        return this.key;
    }


    @Override
    public String toString() {
        if (isDebug) {
            return "Annotation [key=" + key + ", value=" + value + ", threadname=" + threadname + "]";
        }
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
