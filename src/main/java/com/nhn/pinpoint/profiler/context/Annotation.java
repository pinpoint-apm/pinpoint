package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.util.AnnotationValueMapper;
import com.nhn.pinpoint.thrift.dto.TAnnotation;
import com.nhn.pinpoint.thrift.dto.TAnnotationValue;
import com.nhn.pinpoint.thrift.dto.TIntStringStringValue;
import com.nhn.pinpoint.thrift.dto.TIntStringValue;

/**
 * @author netspider
 * @author emeroad
 */
public class Annotation extends TAnnotation {

    public Annotation(int key) {
        super(key);
    }

    public Annotation(int key, Object value) {
        super(key);
        AnnotationValueMapper.mappingValue(this, value);
    }

    public Annotation(int key, TIntStringValue value) {
        super(key);
        this.setValue(TAnnotationValue.intStringValue(value));
    }

    public Annotation(int key, TIntStringStringValue value) {
        super(key);
        this.setValue(TAnnotationValue.intStringStringValue(value));
    }

    public Annotation(int key, String value) {
        super(key);
        this.setValue(TAnnotationValue.stringValue(value));
    }

    public Annotation(int key, int value) {
        super(key);
        this.setValue(TAnnotationValue.intValue(value));
    }

    public int getAnnotationKey() {
        return this.getKey();
    }

}
