package com.nhn.pinpoint.profiler.util;


import com.nhn.pinpoint.thrift.dto.*;
import com.nhn.pinpoint.common.util.TranscoderUtils;

import java.io.UnsupportedEncodingException;

public class AnnotationTranscoder {

    public void mappingValue(Object o, TAnnotation annotation) {
        if (o == null) {
            return;
        }
        if (o instanceof String) {
            annotation.setStringValue((String) o);
            return;
        } else if (o instanceof Integer) {
            annotation.setIntValue((Integer) o);
            return;
        } else if (o instanceof Long) {
            annotation.setLongValue((Long) o);
            return;
        } else if (o instanceof Boolean) {
            annotation.setBoolValue((Boolean) o);
            return;
        } else if (o instanceof Byte) {
            annotation.setByteValue((Byte) o);
            return;
        } else if (o instanceof Float) {
            // thrift는 float가 없음.
            annotation.setDoubleValue((Float) o);
            return;
        } else if (o instanceof Double) {
            annotation.setDoubleValue((Double) o);
            return;
        } else if (o instanceof byte[]) {
            annotation.setBinaryValue((byte[]) o);
            return;
        } else if (o instanceof Short) {
            annotation.setShortValue((Short) o);
            return;
        }
        String str = o.toString();
        annotation.setStringValue(str);
        return;
    }

}
