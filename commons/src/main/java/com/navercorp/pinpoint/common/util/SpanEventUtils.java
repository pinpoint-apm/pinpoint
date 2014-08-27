package com.nhn.pinpoint.common.util;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.thrift.dto.TAnnotation;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;

import java.util.List;

/**
 * @author emeroad
 */
public class SpanEventUtils {

    public static boolean hasException(TSpanEvent spanEvent) {
        if (spanEvent.isSetExceptionInfo()) {
            return true;
        }
        return false;
    }
}
